package org.tigris.subversion.subclipse.graph.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.ui.operations.SVNOperation;
import org.tigris.subversion.sublicpse.graph.cache.Cache;
import org.tigris.subversion.sublicpse.graph.cache.Graph;
import org.tigris.subversion.sublicpse.graph.cache.Node;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class GraphBackgroundTask extends SVNOperation {
	
	private IResource resource;
	private ISVNRemoteResource remoteResource;
	private GraphicalViewer viewer;
	private RevisionGraphEditor editor;
	private SVNRevision refreshRevision;
	private boolean includeMergedRevisions = false;
	private List refreshedMessages;
//	private ISVNLogMessage[] refreshedMessageArray;

	private static final int TOTAL_STEPS = Integer.MAX_VALUE;
	private static final int SHORT_TASK_STEPS = TOTAL_STEPS / 50; // 2%
	private static final int VERY_LONG_TASK = TOTAL_STEPS / 2; // 50%
	private static final int TASK_STEPS = (TOTAL_STEPS - SHORT_TASK_STEPS*3 - VERY_LONG_TASK) / 2;

	protected GraphBackgroundTask(IWorkbenchPart part, GraphicalViewer viewer, RevisionGraphEditor editor) {
		super(part);
		this.viewer = viewer;
		this.editor = editor;
	}	
	
	public GraphBackgroundTask(IWorkbenchPart part, GraphicalViewer viewer, RevisionGraphEditor editor, IResource resource) {
		this(part, viewer, editor);
		this.resource = resource;
	}
	
	public GraphBackgroundTask(IWorkbenchPart part, GraphicalViewer viewer, RevisionGraphEditor editor, ISVNRemoteResource remoteResource) {
		this(part, viewer, editor);
		this.remoteResource = remoteResource;
	}

	protected void execute(IProgressMonitor monitor) throws SVNException,
			InterruptedException {
		Cache cache = null;
		monitor.beginTask("Calculating graph information", TOTAL_STEPS);
		monitor.worked(SHORT_TASK_STEPS);
		try {
			ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClient();
			ISVNInfo info;
			if (resource == null) info = client.getInfo(remoteResource.getUrl());
			else {
				if (resource.getRawLocation() == null) info = client.getInfoFromWorkingCopy(resource.getLocation().toFile());
				else info = client.getInfoFromWorkingCopy(resource.getRawLocation().toFile());
				if (info.getUuid() == null) {
					info = client.getInfo(info.getUrl());
				}
			}
			
			if (editor != null) ((RevisionGraphEditorInput)editor.getEditorInput()).setInfo(info);
			
			long revision = info.getRevision().getNumber();
			String path = info.getUrl().toString().substring(info.getRepository().toString().length());
			
			monitor.setTaskName("Initializating cache");
			cache = getCache(info.getUuid());
			monitor.worked(SHORT_TASK_STEPS);
			
			// update the cache
			long latestRevisionStored = cache.getLatestRevision();
			SVNRevision latest = null;
			SVNRevision endRevision = null;
			monitor.setTaskName("Connecting to the repository");
			// TODO: try-catch this line and make it work off-line
			long latestRevisionInRepository = client.getInfo(info.getRepository()).getLastChangedRevision().getNumber();
			monitor.worked(SHORT_TASK_STEPS);

			if(refreshRevision != null || latestRevisionInRepository > latestRevisionStored) {
				if (refreshRevision == null) {
					if(latestRevisionStored == 0)
						latest = SVNRevision.START;
					else
						latest = new SVNRevision.Number(latestRevisionStored+1);
				} else {
					latest = refreshRevision;
				}
				
				if (refreshRevision == null) endRevision = SVNRevision.HEAD;
				else endRevision = refreshRevision;

				// for now
//				includeMergedRevisions = false;
				
				try {
					monitor.setTaskName("Retrieving revision history");
					int unitWork;
					if (refreshRevision == null) unitWork = VERY_LONG_TASK / (int) (latestRevisionInRepository - latestRevisionStored);
					else unitWork = VERY_LONG_TASK;
					if (refreshRevision != null) {
						refreshedMessages = new ArrayList();
						client.getLogMessages(info.getRepository(),
								latest,
								latest,
								endRevision,
								false, true, 0, includeMergedRevisions,
								ISVNClientAdapter.DEFAULT_LOG_PROPERTIES,
								new ISVNLogMessageCallback() {
									public void singleMessage(
											ISVNLogMessage message) {
										if (message != null) refreshedMessages.add(message);
									}							
								});						
						cache.refresh(refreshedMessages);
					} else {
						cache.startUpdate();
						client.getLogMessages(info.getRepository(),
								latest,
								latest,
								endRevision,
								false, true, 0, includeMergedRevisions,
								ISVNClientAdapter.DEFAULT_LOG_PROPERTIES,
								new CallbackUpdater(cache, monitor, unitWork));
						cache.finishUpdate();
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				monitor.worked(VERY_LONG_TASK);
			}
			if (editor != null) updateView(monitor, cache, path, revision);
			monitor.done();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			
			ISVNLogMessage[] logMessages = cache.getLogMessages();
			for (int i = 0; i < logMessages.length; i++) 
//				System.out.println(logMessages[i].getRevision().getNumber());
			
			if(cache != null)
				cache.close();
			// TODO: clean up ISVNClientAdapter ?
		}
	}
	
//	private void serialize(Graph graph) {
//		try {
//			FileOutputStream fos = new FileOutputStream("c:/sample-graph");
//			ObjectOutputStream oos = new ObjectOutputStream(fos);
//			oos.writeObject(graph);
//			fos.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	private void updateView(IProgressMonitor monitor, Cache cache, String path, long revision) {
		monitor.setTaskName("Finding root node");
		
		int unitWork = TASK_STEPS / (int)(revision);
		if(unitWork < 1) unitWork = 1;
		Node root = cache.findRootNode(path, revision,
				new WorkMonitorListener(monitor, unitWork));
		
		monitor.setTaskName("Calculating graph");
		if(revision == root.getRevision())
			unitWork = TASK_STEPS;
		else
			unitWork = TASK_STEPS / (int)(revision - root.getRevision());
		if(unitWork < 1) unitWork = 1;
		final Graph graph = cache.createGraph(
				root.getPath(),
				root.getRevision(),
				new WorkMonitorListener(monitor, unitWork));
		graph.setSelectedPath(path);
		graph.setSelectedRevision(revision);
		monitor.setTaskName("Drawing graph");
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				viewer.setContents(graph);
			}
		});
	}
	
	private Cache getCache(String uuid) {
		File f = Cache.getCacheDirectory(resource);
		if (refreshRevision == null) return new Cache(f, uuid);
		else return new Cache(f, uuid, Long.parseLong(refreshRevision.toString())); 
	}

	protected String getTaskName() {
		return "Calculating graph information";
	}
	
	public void setRefreshRevision(SVNRevision refreshRevision) {
		this.refreshRevision = refreshRevision;
		includeMergedRevisions = refreshRevision != null;
	}
	
	public void setIncludeMergedRevisions(boolean includeMergedRevisions) {
		this.includeMergedRevisions = includeMergedRevisions;
	}

}

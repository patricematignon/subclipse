package org.tigris.subversion.subclipse.graph.editors;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.ui.operations.SVNOperation;
import org.tigris.subversion.sublicpse.graph.cache.Cache;
import org.tigris.subversion.sublicpse.graph.cache.Graph;
import org.tigris.subversion.sublicpse.graph.cache.Node;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class GraphBackgroundTask extends SVNOperation {
	
	private IResource resource;
	private GraphicalViewer viewer;

	private static final int TOTAL_STEPS = Integer.MAX_VALUE;
	private static final int SHORT_TASK_STEPS = TOTAL_STEPS / 50; // 2%
	private static final int VERY_LONG_TASK = TOTAL_STEPS / 2; // 50%
	private static final int TASK_STEPS = (TOTAL_STEPS - SHORT_TASK_STEPS*3 - VERY_LONG_TASK) / 2;
	
	protected GraphBackgroundTask(IWorkbenchPart part, GraphicalViewer viewer, IResource resource) {
		super(part);
		this.viewer = viewer;
		this.resource = resource;
	}

	protected void execute(IProgressMonitor monitor) throws SVNException,
			InterruptedException {
		Cache cache = null;
		monitor.beginTask("Calculating graph information", TOTAL_STEPS);
		monitor.worked(SHORT_TASK_STEPS);
		try {
			ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClient();
			ISVNInfo info = client.getInfoFromWorkingCopy(resource.getRawLocation().toFile());
			
			long revision = info.getRevision().getNumber();
			String path = info.getUrl().toString().substring(info.getRepository().toString().length());
			
			monitor.setTaskName("Initializating cache");
			cache = getCache(resource, info.getUuid());
			monitor.worked(SHORT_TASK_STEPS);
			
			// update the cache
			long latestRevisionStored = cache.getLatestRevision();
			SVNRevision latest = null;
			monitor.setTaskName("Connecting to the repository");
			// TODO: try-catch this line and make it work off-line
			long latestRevisionInRepository = client.getInfo(info.getRepository()).getRevision().getNumber();
			monitor.worked(SHORT_TASK_STEPS);

			if(latestRevisionInRepository > latestRevisionStored) {
				if(latestRevisionStored == 0)
					latest = SVNRevision.START;
				else
					latest = new SVNRevision.Number(latestRevisionStored+1);

				try {
					monitor.setTaskName("Retrieving revision history");
					int unitWork = VERY_LONG_TASK / (int) (latestRevisionInRepository - latestRevisionStored);
					
					cache.startUpdate();
					client.getLogMessages(info.getRepository(),
							latest,
							latest,
							SVNRevision.HEAD,
							false, true, 0, false,
							ISVNClientAdapter.DEFAULT_LOG_PROPERTIES,
							new CallbackUpdater(cache, monitor, unitWork));
					cache.finishUpdate();
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				monitor.worked(VERY_LONG_TASK);
			}
			updateView(monitor, cache, path, revision);
			monitor.done();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
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
	
	private Cache getCache(IResource file, String uuid) {
		File f = file.getWorkspace().getRoot().getRawLocation().toFile();
		f = new File(f, ".metadata");
		f = new File(f, ".plugins");
		f = new File(f, "org.tigris.subversion.subclipse.graph");
		return new Cache(f, uuid);
	}

	protected String getTaskName() {
		return "Calculating graph information";
	}

}

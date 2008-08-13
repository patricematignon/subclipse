package org.tigris.subversion.subclipse.graph.editors;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.ui.operations.SVNOperation;
import org.tigris.subversion.sublicpse.graph.cache.Cache;
import org.tigris.subversion.sublicpse.graph.cache.Graph;
import org.tigris.subversion.sublicpse.graph.cache.Node;
import org.tigris.subversion.sublicpse.graph.cache.WorkListener;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class RevisionGraphEditor extends EditorPart {

	private ScrollingGraphicalViewer viewer;
	private ActionRegistry actionRegistry;

	public ActionRegistry getActionRegistry() {
		if (actionRegistry == null)
			actionRegistry = new ActionRegistry();
		return actionRegistry;
	}

	public void setFocus() {
	}
	
	public void showGraphFor(IResource resource) {
		setPartName(resource.getName()+" revision graph");
//		setContentDescription("Revision graph for "+resource.getName());
		ShowGraphBackgroundTask task =
			new ShowGraphBackgroundTask(getSite().getPart(), viewer, resource);
		try {
			task.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Object getAdapter(Class adapter) {
		if(adapter == GraphicalViewer.class || adapter == EditPartViewer.class) {
			return viewer;
		} else if(adapter == ZoomManager.class) {
			return ((ScalableRootEditPart) viewer.getRootEditPart()).getZoomManager();
		}
		return super.getAdapter(adapter);
	}
	
	public void createPartControl(Composite parent) {
		GC gc = new GC(parent);
		gc.setAntialias(SWT.ON);
		viewer = new ScrollingGraphicalViewer();
		viewer.createControl(parent);
		ScalableRootEditPart root = new ScalableRootEditPart();
		viewer.setRootEditPart(root);
		viewer.setEditPartFactory(new GraphEditPartFactory());
		viewer.setContents("Loading graph... This can take several minutes");
		IEditorInput input = getEditorInput();
		if(input instanceof FileEditorInput) {
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			showGraphFor(fileEditorInput.getFile());
		} else if(input instanceof RevisionGraphEditorInput) {
			RevisionGraphEditorInput editorInput = (RevisionGraphEditorInput) input;
			showGraphFor(editorInput.getResource());
		}
		
		// zoom stuff
		ZoomManager zoomManager = ((ScalableRootEditPart) viewer.getRootEditPart()).getZoomManager();
		IAction zoomIn = new ZoomInAction(zoomManager);
		IAction zoomOut = new ZoomOutAction(zoomManager);
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);
		// keyboard
		getSite().getKeyBindingService().registerAction(zoomIn); // FIXME, deprecated
		getSite().getKeyBindingService().registerAction(zoomOut); // FIXME, deprecated
		List zoomContributions = Arrays.asList(new String[] { 
			     ZoomManager.FIT_ALL, 
			     ZoomManager.FIT_HEIGHT, 
			     ZoomManager.FIT_WIDTH });
		zoomManager.setZoomLevelContributions(zoomContributions);
		// toolbar
		IToolBarManager mgr = getEditorSite().getActionBars().getToolBarManager();
		mgr.add(new ZoomComboContributionItem(getSite().getPage()));
		// menu
		// mouse wheel
		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1),
				MouseWheelZoomHandler.SINGLETON);
	}

	public void doSave(IProgressMonitor monitor) {
	}

	public void doSaveAs() {
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

} class ShowGraphBackgroundTask extends SVNOperation {
	
	private IResource resource;
	private GraphicalViewer viewer;

	private static final int TOTAL_STEPS = Integer.MAX_VALUE;
	private static final int SHORT_TASK_STEPS = TOTAL_STEPS / 50; // 2%
	private static final int VERY_LONG_TASK = TOTAL_STEPS / 2; // 50%
	private static final int TASK_STEPS = (TOTAL_STEPS - SHORT_TASK_STEPS*3 - VERY_LONG_TASK) / 2;
	
	protected ShowGraphBackgroundTask(IWorkbenchPart part, GraphicalViewer viewer, IResource resource) {
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

} class WorkMonitorListener implements WorkListener {
	
	private IProgressMonitor monitor;
	private int unitWork;
	
	public WorkMonitorListener(IProgressMonitor monitor, int unitWork) {
		this.monitor = monitor;
		this.unitWork = unitWork;
	}

	public void worked() {
		monitor.worked(unitWork);
	}

} class CallbackUpdater implements ISVNLogMessageCallback {
	
	private Cache cache;
	private IProgressMonitor monitor;
	private int unitWork;
	
	public CallbackUpdater(Cache cache, IProgressMonitor monitor, int unitWork) {
		this.cache = cache;
		this.monitor = monitor;
		this.unitWork = unitWork;
	}

	public void singleMessage(ISVNLogMessage message) {
		cache.update(message);
		monitor.worked(unitWork);
	}

} class GraphEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart editPart, Object node) {
		if (node instanceof String) {
			final String s = (String) node;
			return new AbstractGraphicalEditPart() {
				protected IFigure createFigure() {
					return new Label(s);
				}

				protected void createEditPolicies() {
				}
			};
		} else if (node instanceof Graph) {
			return new GraphEditPart((Graph) node);
		}
		throw new RuntimeException("cannot create EditPart for "+node.getClass().getName()+" class");
	}

}
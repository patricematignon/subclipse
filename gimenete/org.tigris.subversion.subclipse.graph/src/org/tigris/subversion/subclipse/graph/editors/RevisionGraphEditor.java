package org.tigris.subversion.subclipse.graph.editors;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
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
import org.tigris.subversion.sublicpse.graph.cache.Node;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class RevisionGraphEditor extends EditorPart {

	private GraphicalViewer viewer;

	public void setFocus() {
	}
	
	public void showGraphFor(IResource resource) {
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
		IToolBarManager mgr = getEditorSite().getActionBars().getToolBarManager();
		mgr.add(new ZoomComboContributionItem(getSite().getPage()));
		
		viewer = new ScrollingGraphicalViewer();
		GC gc = new GC(parent);
		gc.setAntialias(SWT.ON);
		viewer.createControl(parent);
		ScalableRootEditPart root = new ScalableRootEditPart();
		viewer.setRootEditPart(root);
		viewer.setEditPartFactory(new GraphEditPartFactory());
		viewer.setContents("Nothing to show");
		IEditorInput input = getEditorInput();
		if(input instanceof FileEditorInput) {
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			showGraphFor(fileEditorInput.getFile());
		}
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
	
	private final static int MAGIC_NUMBER = 100;
	
	protected ShowGraphBackgroundTask(IWorkbenchPart part, GraphicalViewer viewer, IResource resource) {
		super(part);
		this.viewer = viewer;
		this.resource = resource;
	}

	protected void execute(IProgressMonitor monitor) throws SVNException,
			InterruptedException {
		Cache cache = null;
		try {
			ISVNClientAdapter client = SVNProviderPlugin.getPlugin().getSVNClient();
			ISVNInfo info = client.getInfoFromWorkingCopy(resource.getRawLocation().toFile());
			long revision = info.getRevision().getNumber();
			String path = info.getUrl().toString().substring(info.getRepository().toString().length());
			
			monitor.setTaskName("Initializating cache");
			cache = getCache(resource, info.getUuid());
			
			// update the cache
			long latestRevisionStored = cache.getLatestRevision();
			SVNRevision latest = null;
			if(latestRevisionStored < revision) { // FIXME: this is not always right
				long latestRevisionInRepository = client.getInfo(info.getRepository()).getRevision().getNumber();
				
				if(latestRevisionInRepository > latestRevisionStored) {
					if(latestRevisionStored == 0)
						latest = SVNRevision.START;
					else
						latest = new SVNRevision.Number(latestRevisionStored);
					
					try {
						monitor.setTaskName("Retrieving revision history");
						monitor.beginTask("Asking the repository for updates", 50*2);
						int workedUnit = (int) (50 / ((latestRevisionInRepository - latestRevisionStored) / (float) MAGIC_NUMBER));
						CacheUpdaterThread cacheUpdater = null;
						while(latestRevisionInRepository > latestRevisionStored) {
							latestRevisionStored += MAGIC_NUMBER;
							if(latestRevisionStored > latestRevisionInRepository) {
								latestRevisionStored = latestRevisionInRepository;
							}
							ISVNLogMessage[] messages = client.getLogMessages(info.getRepository(),
									latest, new SVNRevision.Number(latestRevisionStored));
							monitor.worked(workedUnit);
//							printLogMessages(messages);
							if(cacheUpdater != null) {
								cacheUpdater.join();
								monitor.worked(workedUnit);
							}
							cacheUpdater = new CacheUpdaterThread(cache, messages);
							cacheUpdater.start();
							latest = new SVNRevision.Number(latestRevisionStored+1);
						}
						if(cacheUpdater != null) {
							cacheUpdater.join();
						}
						monitor.done();
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("No updates");
				}
			}
			updateView(cache, path, revision);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
//			if(cache != null)
//				cache.close();
			// TODO: clean up ISVNClientAdapter ?
		}
	}
	
	private void updateView(Cache cache, String path, long revision) {
		Long fileId = cache.getFileId(path, revision);
		final List nodes = cache.getNodes(fileId.longValue());
		// printNodes(nodes);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				viewer.setContents(new Graph(nodes));
			}
		});
	}
	
	private Cache getCache(IResource file, String uuid) {
		File database = file.getWorkspace().getRoot().getRawLocation().toFile();
		database = new File(database, ".metadata");
		database = new File(database, ".plugins");
		database = new File(database, "org.tigris.subversion.subclipse.graph");
		database = new File(database, uuid);
		return new Cache(database);
	}

	protected String getTaskName() {
		return "Calculating graph information";
	}

} class CacheUpdaterThread extends Thread {
	
	private Cache cache;
	private ISVNLogMessage[] messages;
	
	public CacheUpdaterThread(Cache cache,
			ISVNLogMessage[] messages) {
		this.cache = cache;
		this.messages = messages;
	}
	
	public void run() {
		cache.update(messages); // TODO: handle exceptions
	}
	

} class Graph {
	
	private List nodes;
	
	public Graph(List nodes) {
		this.nodes = nodes;
	}
	
	public List getNodes() {
		return nodes;
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

} class GraphEditPart extends AbstractGraphicalEditPart {
	
	private Graph graph;

	private final static int NODE_WIDTH = 70;
	private final static int NODE_HEIGHT = 30;
	private final static int BRANCH_WIDTH = 150;
	private final static int BRANCH_HEIGHT = 30;
	private final static int BRANCH_OFFSET = 170;
	private final static int NODE_OFFSET_Y = 40;
	private final static int NODE_OFFSET_X = (BRANCH_WIDTH - NODE_WIDTH) / 2;
	
	public GraphEditPart(Graph graph) {
		this.graph = graph;
	}

	protected IFigure createFigure() {
		List nodes = graph.getNodes();
		Figure contents = new Figure();
		XYLayout contentsLayout = new XYLayout();
		contents.setLayoutManager(contentsLayout);
		
		Map branches = new HashMap();
		Map figures = new HashMap();
		Map previousFigure = new HashMap();
		Map nodesCount = new HashMap();

		int i=0;
		for (Iterator iterator = nodes.iterator(); iterator
				.hasNext();) {
			Node node = (Node) iterator.next();
			NodeFigure figure = new NodeFigure(node);
			figures.put(node.getRevision()+"@"+node.getPath(), figure);
			
			int branchNodesCount = 0;
			
			BranchFigure branch = (BranchFigure) branches.get(node.getPath());
			if(branch == null) {
				branch = new BranchFigure(node.getPath());
				contentsLayout.setConstraint(branch,
						new Rectangle(10+branches.keySet().size()*BRANCH_OFFSET, 10, BRANCH_WIDTH, BRANCH_HEIGHT));
				branches.put(node.getPath(), branch);
				contents.add(branch);
				previousFigure.put(node.getPath(), branch);
				nodesCount.put(node.getPath(), new Integer(0));
			} else {
				branchNodesCount = ((Integer) nodesCount.get(node.getPath())).intValue() + 1;
				nodesCount.put(node.getPath(), new Integer(branchNodesCount));
			}
			Rectangle r = (Rectangle) contentsLayout.getConstraint(branch);
			contentsLayout.setConstraint(figure,
					new Rectangle(NODE_OFFSET_X+r.x, 50+NODE_OFFSET_Y*branchNodesCount, NODE_WIDTH, NODE_HEIGHT));
			contents.add(figure);

			ChopboxAnchor sourceAnchor = null;
			if(node.getCopySrcPath() == null) {
				sourceAnchor = new ChopboxAnchor((Figure) previousFigure.get(node.getPath()));
			} else {
				Figure source = (Figure) figures.get(node.getCopySrcRevision()+"@"+node.getCopySrcPath());
				if(source != null) {
					sourceAnchor = new ChopboxAnchor(source);
				} else {
					long rev = node.getCopySrcRevision();
					do { // FIXME: this is very ugly
						source = (Figure) figures.get(rev+"@"+node.getCopySrcPath());
						rev--;
					} while(source == null && rev >= 0);
					if(source != null) {
						sourceAnchor = new ChopboxAnchor(source);
					} else {
						sourceAnchor = new ChopboxAnchor((Figure) previousFigure.get(node.getCopySrcPath()));
					}
				}
			}
			if(sourceAnchor != null) {
				PolylineConnection c = new PolylineConnection();
//				c.setLineWidth(2);
//				c.setConnectionRouter(new ManhattanConnectionRouter());
				ChopboxAnchor targetAnchor = new ChopboxAnchor(figure);
				c.setSourceAnchor(sourceAnchor);
				c.setTargetAnchor(targetAnchor);
				PolygonDecoration decoration = new PolygonDecoration();
				decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
				c.setSourceDecoration(decoration);
				contents.add(c);
			}
			
			previousFigure.put(node.getPath(), figure);
			
			i++;
		}
		
		return contents;
	}

	protected void createEditPolicies() {
	}
	
}
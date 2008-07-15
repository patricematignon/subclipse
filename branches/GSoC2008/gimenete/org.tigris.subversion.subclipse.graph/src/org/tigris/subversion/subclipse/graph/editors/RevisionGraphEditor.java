package org.tigris.subversion.subclipse.graph.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
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
import org.tigris.subversion.sublicpse.graph.cache.Node;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class RevisionGraphEditor extends EditorPart {

	private GraphicalViewer viewer;
	private ActionRegistry actionRegistry;

	public ActionRegistry getActionRegistry() {
		if (actionRegistry == null)
			actionRegistry = new ActionRegistry();
		return actionRegistry;
	}

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
		GC gc = new GC(parent);
		gc.setAntialias(SWT.ON);
		viewer = new ScrollingGraphicalViewer();
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

	private final static int NODE_WIDTH = 50;
	private final static int NODE_HEIGHT = 20;
	private final static int NODE_HEIGHT2 = NODE_HEIGHT / 2;
	private final static int BRANCH_WIDTH = 200;
	private final static int BRANCH_HEIGHT = 30;
	private final static int BRANCH_OFFSET = BRANCH_WIDTH+20;
	private final static int NODE_OFFSET_Y = 10;
	private final static int NODE_OFFSET_X = (BRANCH_WIDTH - NODE_WIDTH) / 2;
	private final static int ARROW_PADDING = 10;
	
	private static final String TAG_PREFIX = "/tags/";
	private boolean hideTags = true;
	
	public GraphEditPart(Graph graph) {
		this.graph = graph;
	}

	protected IFigure createFigure() {
		List nodes = graph.getNodes();
		Figure contents = new Figure();
		XYLayout contentsLayout = new XYLayout();
		contents.setLayoutManager(contentsLayout);
		
		Map branches = new HashMap();
		List branchesList = new ArrayList();
		List branchesPathList = new ArrayList();

		for (Iterator iterator = nodes.iterator(); iterator
				.hasNext();) {
			Node node = (Node) iterator.next();
			
			NodeFigure figure = new NodeFigure(node);
			Branch branch = (Branch) branches.get(node.getPath());
			if(branch == null) {
				branch = new Branch(new BranchFigure(node.getPath()));
				branches.put(node.getPath(), branch);
				branchesList.add(branch);
				branchesPathList.add(node.getPath());
				contents.add(branch.getBranchFigure());
			}
			
			NodeFigure source = null;
			
			ChopboxAnchor sourceAnchor = null;
			if(node.getCopySrcPath() == null) {
				sourceAnchor = new ChopboxAnchor(branch.getLast());
			} else {
				Node n = node;
				do {
					source = ((Branch) branches.get(n.getCopySrcPath())).get(n.getCopySrcRevision());
					n = source.getNode();
				} while(isTag(n.getPath()));

				if(!isTag(node.getPath()))
					sourceAnchor = new ChopboxAnchor(source);
			}
			
			/*
			if(branch.getNodes().isEmpty()) {
				PolylineConnection c = new PolylineConnection();
				c.setSourceAnchor(new ChopboxAnchor(figure));
				c.setTargetAnchor(new ChopboxAnchor(branch.getBranchFigure()));
				c.setLineStyle(Graphics.LINE_DASHDOT);
				contents.add(c);
			}
			*/
			
			branch.addNode(figure);
			
			if(!isTag(node.getPath())) {
				contents.add(figure);
				
				PolylineConnection c = new PolylineConnection();
				ConnectionAnchor targetAnchor = new ChopboxAnchor(figure);
				c.setSourceAnchor(sourceAnchor);
				c.setTargetAnchor(targetAnchor);
				PolygonDecoration decoration = new PolygonDecoration();
				decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
				c.setSourceDecoration(decoration);
				contents.add(c);

				if(source != null) {
					int i = source.addConnection(c, node);
					figure.setSource(c, i);
				}
			} else if(source != null) {
				source.addTag(node);
			} else {
				System.out.println("meeeec");
				System.out.println(node.getRevision()+", "+node.getPath()+", "+node.getCopySrcPath()+", "+node.getCopySrcRevision());
			}
		}
		
		int branchIndex = 0;
		for (Iterator it = branchesList.iterator(); it.hasNext(); branchIndex++) {
			Branch branch = (Branch) it.next();
			
			if(isTag(branch.getBranchFigure().getPath())) {
				branchIndex--;
				continue;
			}
			
			Rectangle rect = new Rectangle(10+branchIndex*BRANCH_OFFSET, 10, BRANCH_WIDTH, BRANCH_HEIGHT);
			contentsLayout.setConstraint(branch.getBranchFigure(), rect);
			
			int x = rect.x + NODE_OFFSET_X;
			int figureIndex = 0;
			for (Iterator iter = branch.getNodes().iterator(); iter.hasNext(); figureIndex++) {
				NodeFigure figure = (NodeFigure) iter.next();

				int y = NODE_OFFSET_Y + rect.y + rect.height;
				int height = NODE_HEIGHT + ARROW_PADDING * figure.getConnections().size();
				
				/*
				if(figure.getSource() != null && figureIndex == 0) {
					NodeFigure source = (NodeFigure) figure.getSource().getSourceAnchor().getOwner();
					Rectangle r = (Rectangle) contentsLayout.getConstraint(source);
					y = r.y + NODE_HEIGHT * figure.getSourceIndex();
					
					Point point = new Point(x, y + height/2);
					XYAnchor anchor = new MyXYAnchor(point, source);
					figure.getSource().setTargetAnchor(anchor);
				}
				*/
				rect = new Rectangle(x, y, NODE_WIDTH, height);
				contentsLayout.setConstraint(figure, rect);
				
				int k = 0;
				for (Iterator i = figure.getConnections().iterator(); i
						.hasNext(); k++) {
					PolylineConnection c = (PolylineConnection) i.next();
					NodeFigure source = (NodeFigure) c.getTargetAnchor().getOwner();
					int index = branchesPathList.indexOf(source.getNode().getPath());
					int px = x;
					if(index > branchIndex) {
						px += rect.width;
					}
					Point point = new Point(px, y+NODE_HEIGHT2+k*ARROW_PADDING);
					XYAnchor anchor = new MyXYAnchor(point, figure);
					c.setSourceAnchor(anchor);
				}
				figure.endLayout();
			}
		}
		
		return contents;
	}
	
	private boolean isTag(String path) {
		if(path == null) return false;
		return hideTags && path.startsWith(TAG_PREFIX);
	}

	protected void createEditPolicies() {
	}

} class MyXYAnchor extends XYAnchor {
	
	private IFigure f;

	public MyXYAnchor(Point point, IFigure f) {
		super(point);
		this.f = f;
	}
	
	public Point getLocation(Point reference) {
		Point p = super.getLocation(reference).getCopy();
		f.translateToAbsolute(p);
		return p;
	}
	
	public IFigure getOwner() {
		return f;
	}
	
} class Branch {
	
	private static final Comparator c = new Comparator() {
		public int compare(Object a, Object b) {
			long ra;
			long rb;
			if(a instanceof Long) {
				ra = ((Long) a).longValue();
			} else if(a instanceof NodeFigure) {
				ra = ((NodeFigure) a).getNode().getRevision();
			} else {
				throw new RuntimeException();
			}
			if(b instanceof Long) {
				rb = ((Long) b).longValue();
			} else if(b instanceof NodeFigure) {
				rb = ((NodeFigure) b).getNode().getRevision();
			} else {
				throw new RuntimeException();
			}
			if(ra < rb) {
				return -1;
			} else if(ra > rb) {
				return 1;
			}
			return 0;
		}
	};
	
	private BranchFigure branch;
	private List nodes = new ArrayList();
	private Figure last = null;
	
	public Branch(BranchFigure branch) {
		this.branch = branch;
		this.last = branch;
	}
	
	public void addNode(NodeFigure f) {
		nodes.add(f);
		last = f;
	}
	
	public Figure getLast() {
		return last;
	}
	
	public NodeFigure get(long revision) {
		int index = Collections.binarySearch(nodes, new Long(revision), c);
		if(index < 0) {
			index = -index-2;
			if(index < 0) {
				return null;
			}
		}
		return (NodeFigure) nodes.get(index);
	}
	
	public BranchFigure getBranchFigure() {
		return branch;
	}
	
	public List getNodes() {
		return nodes;
	}
	
}
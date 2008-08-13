package org.tigris.subversion.subclipse.graph.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.Color;
import org.tigris.subversion.sublicpse.graph.cache.Branch;
import org.tigris.subversion.sublicpse.graph.cache.Graph;
import org.tigris.subversion.sublicpse.graph.cache.Node;

public class GraphEditPart extends AbstractGraphicalEditPart {
	
	private Graph graph;
	
	public static final Color CONNECTION_COLOR = new Color(null, 172, 182, 198);

	private final static int NODE_WIDTH = 50;
	private final static int NODE_HEIGHT = 30;
	private final static int BRANCH_WIDTH = 220;
	private final static int BRANCH_HEIGHT = 30;
	private final static int BRANCH_OFFSET = BRANCH_WIDTH+20;
	private final static int NODE_OFFSET_Y = 10;
	private final static int NODE_OFFSET_X = (BRANCH_WIDTH - NODE_WIDTH) / 2;
	
	public GraphEditPart(Graph graph) {
		this.graph = graph;
	}

	protected IFigure createFigure() {
		Figure contents = new Figure();
		contents.setBackgroundColor(ColorConstants.white);
		contents.setOpaque(true);
		XYLayout contentsLayout = new XYLayout();
		contents.setLayoutManager(contentsLayout);
		
		List paths = new ArrayList();
		paths.addAll(graph.getPaths());
		paths.remove(graph.getRootPath());
		paths.add(0, graph.getRootPath());
		
		int i = 0;
		// create nodes
		for (Iterator iter = paths.iterator(); iter.hasNext(); i++) {
			String path = (String) iter.next();

			Branch branch = graph.getBranch(path);
			if(branch.getNodes().size() == 1) {
				Node firstNode = (Node) branch.getNodes().iterator().next();
				if(firstNode.getSource() != null && firstNode.getChildCount() == 0) {
					// is not the root node and is not the target of any arrow
					// therefore is a tag
					i--;
					continue;
				}
			}
			
			BranchFigure branchFigure = new BranchFigure(path);
			branch.setView(branchFigure);
			contents.add(branchFigure);

			Rectangle rect = new Rectangle(10+i*BRANCH_OFFSET, 10, BRANCH_WIDTH, BRANCH_HEIGHT);
			contentsLayout.setConstraint(branchFigure, rect);
			
			int x = rect.x + NODE_OFFSET_X;
			int figureIndex = 0;
			for (Iterator it = branch.getNodes().iterator(); it.hasNext(); figureIndex++) {
				Node node = (Node) it.next();

				int y = NODE_OFFSET_Y + rect.y + rect.height;
				int height = NODE_HEIGHT; // + ARROW_PADDING * node.getChildCount();
				
				NodeFigure nodeFigure = new NodeFigure(node);
				contents.add(nodeFigure);
				node.setView(nodeFigure);
				
				rect = new Rectangle(x, y, NODE_WIDTH, height);
				contentsLayout.setConstraint(nodeFigure, rect);
			}
		}
		
		// create connections
		for (Iterator iter = paths.iterator(); iter.hasNext(); i++) {
			String path = (String) iter.next();
			Branch branch = graph.getBranch(path);
			if(branch.getView() == null) {
				for (Iterator it = branch.getNodes().iterator(); it.hasNext();) {
					Node node = (Node) it.next();
					if(node.getSource() != null && node.getSource().getView() != null) {
						NodeFigure nodeFigure = (NodeFigure) node.getSource().getView();
						nodeFigure.addTag(node);
					}
				}
			} else {
				for (Iterator it = branch.getNodes().iterator(); it.hasNext();) {
					Node node = (Node) it.next();
					NodeFigure nodeFigure = (NodeFigure) node.getView();

					if(node.getParent() != null) {
						NodeFigure target = (NodeFigure) node.getParent().getView();
						if(target != null) {
							makeConnection(contents, target, nodeFigure);
						}
					} else if(node.getSource() != null) {
						NodeFigure target = (NodeFigure) node.getSource().getView();
						if(target != null) {
							makeConnection(contents, target, nodeFigure);
//							PolylineConnection c = 
//							target.addConnection(c, node);
						}
					} else {
						makeConnection(contents, (BranchFigure) branch.getView(), nodeFigure);
					}
				}
			}
		}
		
		// end layouts
		for (Iterator iter = paths.iterator(); iter.hasNext(); i++) {
			String path = (String) iter.next();
			Branch branch = graph.getBranch(path);
			if(branch.getView() == null) {
				continue;
			}
			for (Iterator it = branch.getNodes().iterator(); it.hasNext();) {
				Node node = (Node) it.next();
				NodeFigure nodeFigure = (NodeFigure) node.getView();
				nodeFigure.endLayout();
			}
			
		}
		
		return contents;
	}
	
	private PolylineConnection makeConnection(IFigure contents, IFigure source, IFigure target) {
		PolylineConnection c = new PolylineConnection();
		ConnectionAnchor targetAnchor = new ChopboxAnchor(target);
		c.setTargetAnchor(targetAnchor);
		c.setSourceAnchor(new ChopboxAnchor(source));
		PolygonDecoration decoration = new PolygonDecoration();
		decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
		c.setTargetDecoration(decoration);
		c.setForegroundColor(CONNECTION_COLOR);
		contents.add(c);
		return c;
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
	
}
package org.tigris.subversion.subclipse.graph.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.tigris.subversion.sublicpse.graph.cache.Branch;
import org.tigris.subversion.sublicpse.graph.cache.Graph;
import org.tigris.subversion.sublicpse.graph.cache.Node;

public class GraphEditPart extends AbstractGraphicalEditPart {
	
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
	
	public GraphEditPart(Graph graph) {
		this.graph = graph;
	}

	protected IFigure createFigure() {
		Figure contents = new Figure();
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
			// TODO: if is tag, i--; continue;
			Branch branch = graph.getBranch(path);
			if(branch.getNodes().size() == 1) {
				Node firstNode = (Node) branch.getNodes().iterator().next();
				if(firstNode.getParent() != null && firstNode.getChildCount() == 0) {
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
				int height = NODE_HEIGHT + ARROW_PADDING * node.getChildCount();
				
				NodeFigure nodeFigure = new NodeFigure(node);
				contents.add(nodeFigure);
				node.setView(nodeFigure);
				
				rect = new Rectangle(x, y, NODE_WIDTH, height);
				contentsLayout.setConstraint(nodeFigure, rect);
				
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
				
				/*
				int k = 0;
				for (Iterator itt = node.getConnections().iterator(); itt
						.hasNext(); k++) {
					PolylineConnection c = (PolylineConnection) itt.next();
					NodeFigure source = (NodeFigure) c.getTargetAnchor().getOwner();
					int index = branchesPathList.indexOf(source.getNode().getPath());
					int px = x;
					if(index > branchIndex) {
						px += rect.width;
					}
					Point point = new Point(px, y+NODE_HEIGHT2+k*ARROW_PADDING);
					XYAnchor anchor = new MyXYAnchor(point, node);
					c.setSourceAnchor(anchor);
				}
				*/
				nodeFigure.endLayout();
			}
		}
		
		// create connections
		for (Iterator iter = paths.iterator(); iter.hasNext(); i++) {
			String path = (String) iter.next();
			Branch branch = graph.getBranch(path);
			if(branch.getView() == null) continue;

			int figureIndex = 0;
			for (Iterator it = branch.getNodes().iterator(); it.hasNext(); figureIndex++) {
				Node node = (Node) it.next();
				NodeFigure nodeFigure = (NodeFigure) node.getView();
				
				ChopboxAnchor sourceAnchor = null;
				
				if(node.getParent() != null) {
					NodeFigure target = (NodeFigure) node.getParent().getView();
					if(target != null) {
						sourceAnchor = new ChopboxAnchor(target);
					}
				} else {
					sourceAnchor = new ChopboxAnchor((BranchFigure) branch.getView());
				}

				if(sourceAnchor != null) {
					PolylineConnection c = new PolylineConnection();
					ConnectionAnchor targetAnchor = new ChopboxAnchor(nodeFigure);
					c.setSourceAnchor(sourceAnchor);
					c.setTargetAnchor(targetAnchor);
					PolygonDecoration decoration = new PolygonDecoration();
					decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
					c.setTargetDecoration(decoration);
					contents.add(c);
				}
			}
		}
		
		
		return contents;
	}
	
	/*
	public IFigure old() {
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
			* /
			
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
				* /
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
		// TODO: take a node as parameter and return false if node.getCopySrcPath() != null
		if(path == null) return false;
		return hideTags && path.startsWith(TAG_PREFIX);
	}
	*/

	protected void createEditPolicies() {
	}

}
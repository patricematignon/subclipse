package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.tigris.subversion.sublicpse.graph.cache.Node;

public class NodeFigure extends RoundedRectangle {

	private static final Color BGCOLOR = new Color(null, 216, 228, 248);
	private static final Color FGCOLOR = new Color(null, 172, 182, 198);
	private static final Color FONT_COLOR = new Color(null, 1, 70, 122);

	private static Font revisionFont;
	private static Font revisionFontBold;
	
	private Node node;
	private PolylineConnection source;
	private int sourceIndex;
	private boolean hasTags;
	
//	private List connections = null;
	
	public NodeFigure(Node node) {
		this.node = node;
		setLayoutManager(new BorderLayout());
		setBackgroundColor(BGCOLOR);
		setForegroundColor(FGCOLOR);
		setOpaque(true);

		if(revisionFont == null) {
			revisionFont = new Font(null, "Arial", 10, SWT.NONE);
			revisionFontBold = new Font(null, "Arial", 10, SWT.BOLD);
		}

		setToolTip(new NodeTooltipFigure(node));
	}
	
	public PolylineConnection getSource() {
		return source;
	}

	public void setSource(PolylineConnection source, int sourceIndex) {
		this.source = source;
		this.sourceIndex = sourceIndex;
	}
	
	public int getSourceIndex() {
		return sourceIndex;
	}

	public Node getNode() {
		return node;
	}
	
//	public int addConnection(PolylineConnection c, Node source) {
//		if(connections == null)
//			connections = new ArrayList();
//		connections.add(c);
//		NodeTooltipFigure tt = (NodeTooltipFigure) getToolTip();
//		tt.addSource(source);
//		return connections.size();
//	}
//	
//	public List getConnections() {
//		return connections;
//	}
	
	public void addTag(Node source) {
		NodeTooltipFigure tt = (NodeTooltipFigure) getToolTip();
		tt.addTag(source);
		this.hasTags = true;
		setForegroundColor(ColorConstants.black);
	}
	
	public void endLayout() {
		if(hasTags)
			add(createLabel(Long.toString(node.getRevision()), revisionFontBold), BorderLayout.CENTER);
		else
			add(createLabel(Long.toString(node.getRevision()), revisionFont), BorderLayout.CENTER);
		NodeTooltipFigure tt = (NodeTooltipFigure) getToolTip();
		tt.endLayout();
	}
	
	public static Label createLabel(String text, Font font) {
		Label label = new Label(text);
		label.setFont(font);
		label.setForegroundColor(FONT_COLOR);
		return label;
	}
	
}

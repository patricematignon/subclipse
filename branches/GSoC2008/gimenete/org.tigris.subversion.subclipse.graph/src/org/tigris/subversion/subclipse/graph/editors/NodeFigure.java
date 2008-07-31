package org.tigris.subversion.subclipse.graph.editors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.tigris.subversion.sublicpse.graph.cache.Node;

public class NodeFigure extends Figure {
	
	public static final Color BGCOLOR = new Color(null, 255, 255, 206);
	public static final Color RED = new Color(null, 247, 92, 61);
	public static final Color GREEN = new Color(null, 153, 231, 51);
	public static final Color BLUE = new Color(null, 54, 175, 245);

	private static Font revisionFont;
	private static Font revisionFontBold;
	
	private Node node;
	private PolylineConnection source;
	private int sourceIndex;
	private boolean hasTags;
	
	private List connections = new ArrayList();
	
	public NodeFigure(Node node) {
		this.node = node;
		setLayoutManager(new BorderLayout());
		switch(node.getAction()) {
		case 'A':
			setBorder(new LineBorder(GREEN, 1));
			break;
		case 'M':
			setBorder(new LineBorder(BLUE, 1));
			break;
		case 'D':
			setBorder(new LineBorder(RED, 1));
		}
		setBackgroundColor(BGCOLOR);
		setOpaque(true);

		if(revisionFont == null) {
			revisionFont = new Font(null, "Arial", 10, SWT.NONE);
			revisionFontBold = new Font(null, "Arial", 10, SWT.BOLD);
		}

		setToolTip(new ToolTipFigure(node));
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
	
	public int addConnection(PolylineConnection c, Node source) {
		connections.add(c);
		ToolTipFigure tt = (ToolTipFigure) getToolTip();
		tt.addSource(source);
		return connections.size();
	}
	
	public void addTag(Node source) {
		ToolTipFigure tt = (ToolTipFigure) getToolTip();
		tt.addTag(source);
		this.hasTags = true;
		((LineBorder) getBorder()).setWidth(2);
	}
	
	public void endLayout() {
		if(hasTags)
			add(createLabel(Long.toString(node.getRevision()), revisionFontBold), BorderLayout.CENTER);
		else
			add(createLabel(Long.toString(node.getRevision()), revisionFont), BorderLayout.CENTER);
		ToolTipFigure tt = (ToolTipFigure) getToolTip();
		tt.endLayout();
	}
	
	public List getConnections() {
		return connections;
	}
	
	public static Label createLabel(String text, Font font) {
		Label label = new Label(text);
		label.setFont(font);
		return label;
	}
	
} class ToolTipFigure extends Figure {

	private static final int BORDER_WIDTH = 5;
	private static final int BORDER_WIDTH2 = BORDER_WIDTH*2;
	private static DateFormat dateFormat;
	private static Font info = null;
	private static Font authorFont = null;
	private static Font dateFont = null;
	private static Font plain = null;

	private boolean hasSources = false;
	private boolean hasTags = false;
	
	public ToolTipFigure(Node node) {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setStretchMinorAxis(false);
		setLayoutManager(layout);	
		setBackgroundColor(NodeFigure.BGCOLOR);
		setOpaque(true);
		layout.setSpacing(5);
		
		// lazy loading and reuse
		if(dateFormat == null) {
			dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
			info = new Font(null, "Arial", 10, SWT.BOLD);
			authorFont = new Font(null, "Arial", 8, SWT.BOLD);
			dateFont = new Font(null, "Arial", 8, SWT.ITALIC);
			plain = new Font(null, "Arial", 8, SWT.NONE);
		}

		Figure tooltip = new Figure();
		setToolTip(tooltip);

		add(createLabel("Action and path", info, NodeFigure.BLUE));
		add(createLabel(node.getAction()+" "+node.getPath(), plain));
		add(createLabel("Author", info, NodeFigure.BLUE));
		add(createLabel(node.getAuthor(), authorFont));
		add(createLabel("Date", info, NodeFigure.BLUE));
		add(createLabel(dateFormat.format(node.getRevisionDate()), dateFont));
		add(createLabel("Message", info, NodeFigure.BLUE));
		add(createLabel(node.getMessage(), dateFont));
		if(node.getCopySrcPath() != null) {
			add(createLabel("From", info, NodeFigure.BLUE));
			add(createLabel(format(node.getCopySrcRevision(), node.getCopySrcPath()), plain));
		}
	}
	
	public void endLayout() {
		Dimension d = getPreferredSize();
		
		setPreferredSize(d.width+BORDER_WIDTH2, d.height+BORDER_WIDTH2);
		setBorder(new LineBorder(NodeFigure.BGCOLOR, BORDER_WIDTH));
	}
	
	public void addSource(Node node) {
		if(!hasSources) {
			add(createLabel("Source of", info, NodeFigure.BLUE));
			hasSources = true;
		}
		add(createLabel(format(node.getRevision(), node.getPath()), plain));
	}
	
	public void addTag(Node node) {
		if(!hasTags) {
			add(createLabel("Tagged as", info, NodeFigure.BLUE));
			hasTags = true;
		}
		add(createLabel(format(node.getRevision(), node.getPath()), plain));
	}
	
	public String format(long revision, String path) {
		return "rev"+Long.toString(revision)+" "+path;
	}
	
	public static Label createLabel(String text, Font font) {
		Label label = new Label(text);
		label.setFont(font);
		label.setTextAlignment(PositionConstants.LEFT);
		return label;
	}
	
	public static Label createLabel(String text, Font font, Color c) {
		Label label = new Label(text);
		label.setFont(font);
		label.setTextAlignment(PositionConstants.LEFT);
		label.setForegroundColor(c);
		return label;
	}
	
	
}

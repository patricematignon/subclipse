package org.tigris.subversion.subclipse.graph.editors;

import java.text.SimpleDateFormat;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
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
	
	public NodeFigure(Node node) {
		setLayoutManager(new BorderLayout());
		switch(node.getAction()) {
		case 'A':
			setBorder(new LineBorder(GREEN, 2));
			break;
		case 'M':
			setBorder(new LineBorder(BLUE, 2));
			break;
		case 'D':
			setBorder(new LineBorder(RED, 2));
		}
		setBackgroundColor(BGCOLOR);
		setOpaque(true);

		Font revisionFont = new Font(null, "Arial", 12, SWT.BOLD);

		setToolTip(new ToolTipFigure(node));
		
		add(createLabel(Long.toString(node.getRevision()), revisionFont), BorderLayout.CENTER);
	}
	
	public static Label createLabel(String text, Font font) {
		Label label = new Label(text);
		label.setFont(font);
		return label;
	}
	
} class ToolTipFigure extends Figure {

	private static final int BORDER_WIDTH = 5;
	private static final int BORDER_WIDTH2 = BORDER_WIDTH*2;
	private static SimpleDateFormat dateFormat;
	private static Font info = null;
	private static Font authorFont = null;
	private static Font dateFont = null;
	
	public ToolTipFigure(Node node) {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setStretchMinorAxis(false);
		setLayoutManager(layout);	
//		setBorder(new LineBorder(ColorConstants.black,1));
		setBackgroundColor(NodeFigure.BGCOLOR);
		setOpaque(true);
		layout.setSpacing(5);
		
		// lazy loading and reuse
		if(dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			info = new Font(null, "Arial", 12, SWT.BOLD);
			authorFont = new Font(null, "Arial", 10, SWT.BOLD);
			dateFont = new Font(null, "Arial", 10, SWT.ITALIC);
		}

		Figure tooltip = new Figure();
		setToolTip(tooltip);

		add(createLabel("Author", info, NodeFigure.BLUE));
		add(createLabel(node.getAuthor(), authorFont));
		add(createLabel("Date", info, NodeFigure.BLUE));
		add(createLabel(dateFormat.format(node.getRevisionDate()), dateFont));
		add(createLabel("Message", info, NodeFigure.BLUE));
		add(createLabel(node.getMessage(), dateFont));
		
		Dimension d = getPreferredSize();
		
		setPreferredSize(d.width+BORDER_WIDTH2, d.height+BORDER_WIDTH2);
		setBorder(new LineBorder(NodeFigure.BGCOLOR, BORDER_WIDTH));
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

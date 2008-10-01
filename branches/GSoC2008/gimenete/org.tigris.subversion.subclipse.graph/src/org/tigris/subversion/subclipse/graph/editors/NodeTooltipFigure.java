package org.tigris.subversion.subclipse.graph.editors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.tigris.subversion.sublicpse.graph.cache.Node;

public class NodeTooltipFigure extends Figure {
	
	private static final Color BGCOLOR = new Color(null, 250, 250, 250);
	private static final Color FONT_COLOR = new Color(null, 1, 70, 122);

	private static final int BORDER_WIDTH = 5;
	private static final int BORDER_WIDTH2 = BORDER_WIDTH*2;
	
	private static DateFormat dateFormat;

	private boolean hasSources = false;
	private boolean hasTags = false;
	
	public NodeTooltipFigure(Node node) {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setStretchMinorAxis(false);
		setLayoutManager(layout);	
		setBackgroundColor(BGCOLOR);
		setOpaque(true);
		layout.setSpacing(5);
		
		// lazy loading and reuse
		if(dateFormat == null) {
			dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		}

		Figure tooltip = new Figure();
		setToolTip(tooltip);

		add(createLabel("Action and path", JFaceResources.getHeaderFont(), FONT_COLOR));
		add(createLabel(node.getAction()+" "+node.getPath(), JFaceResources.getTextFont()));
		add(createLabel("Author", JFaceResources.getHeaderFont(), FONT_COLOR));
		add(createLabel(node.getAuthor(), JFaceResources.getTextFont()));
		add(createLabel("Date", JFaceResources.getHeaderFont(), FONT_COLOR));
		add(createLabel(dateFormat.format(node.getRevisionDate()), JFaceResources.getTextFont()));
		add(createLabel("Message", JFaceResources.getHeaderFont(), FONT_COLOR));
		add(createLabel(node.getMessage(), JFaceResources.getTextFont()));
		if(node.getCopySrcPath() != null) {
			add(createLabel("From", JFaceResources.getHeaderFont(), FONT_COLOR));
			add(createLabel(format(node.getCopySrcRevision(), node.getCopySrcPath()), JFaceResources.getTextFont()));
		}
	}
	
	public void endLayout() {
		Dimension d = getPreferredSize();
		
		setPreferredSize(d.width+BORDER_WIDTH2, d.height+BORDER_WIDTH2);
		setBorder(new LineBorder(BGCOLOR, BORDER_WIDTH));
	}
	
	public void addSource(Node node) {
		if(!hasSources) {
			add(createLabel("Source of", JFaceResources.getHeaderFont(), FONT_COLOR));
			hasSources = true;
		}
		add(createLabel(format(node.getRevision(), node.getPath()), JFaceResources.getTextFont()));
	}
	
	public void addTag(Node node) {
		if(!hasTags) {
			add(createLabel("Tagged as", JFaceResources.getHeaderFont(), FONT_COLOR));
			hasTags = true;
		}
		add(createLabel(format(node.getRevision(), node.getPath()), JFaceResources.getTextFont()));
	}
	
	public String format(long revision, String path) {
		return "r"+Long.toString(revision)+" "+path;
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

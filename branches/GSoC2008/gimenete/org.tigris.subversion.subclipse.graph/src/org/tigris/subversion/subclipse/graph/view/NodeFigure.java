package org.tigris.subversion.subclipse.graph.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class NodeFigure extends Figure {
	
	public static Color classColor = new Color(null,255,255,206);
	
	public NodeFigure(long revision, String author, Date date) {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);	
		setBorder(new LineBorder(ColorConstants.black,1));
		setBackgroundColor(classColor);
		setOpaque(true);

		Font revisionFont = new Font(null, "Arial", 10, SWT.BOLD);
		Font authorFont = new Font(null, "Arial", 10, SWT.BOLD);
		Font dateFont = new Font(null, "Arial", 10, SWT.ITALIC);

		add(createLabel(Long.toString(revision), revisionFont));
		add(createLabel(author, authorFont));
		add(createLabel(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date), dateFont));
	}
	
	private Label createLabel(String text, Font font) {
		Label label = new Label(text);
		label.setFont(font);
		return label;
	}
	
}

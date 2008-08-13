package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.swt.graphics.Color;

public class BranchFigure extends RoundedRectangle {

//	public static final Color BGCOLOR = new Color(null, 255, 255, 206);
	private static final Color BGCOLOR = new Color(null, 216, 228, 248);
	private static final Color FGCOLOR = new Color(null, 172, 182, 198);
	private static final Color FONT_COLOR = new Color(null, 1, 70, 122);
	
	private String path;
	
	public BranchFigure(String path) {
		this.path = path;
		
		setLayoutManager(new BorderLayout());
		setBackgroundColor(BGCOLOR);
		setForegroundColor(FGCOLOR);
		setOpaque(true);

		Label label = new Label(path);
		label.setForegroundColor(FONT_COLOR);
		add(label, BorderLayout.CENTER);
		
		Label tooltip = new Label(path);
		setToolTip(tooltip);
//		tooltip.setBorder(new LineBorder(BGCOLOR, 10));
//		Dimension d = tooltip.getPreferredSize();
//		tooltip.setPreferredSize(d.width+5, d.height+5);
	}
	
	public String getPath() {
		return path;
	}
	
}

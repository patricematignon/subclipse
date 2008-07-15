package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.graphics.Color;

public class BranchFigure extends Figure {
	
	public static final Color BGCOLOR = new Color(null,255,255,206);
	
	private String path;
	
	public BranchFigure(String path) {
		this.path = path;
		
		setLayoutManager(new BorderLayout());	
		setBorder(new LineBorder(ColorConstants.black,1));
		setBackgroundColor(BGCOLOR);
		setOpaque(true);

		Label label = new Label(path);
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

package org.tigris.subversion.subclipse.graph.view;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;

public class BranchFigure extends Figure {
	
	public static Color classColor = new Color(null,255,255,206);
	
	public BranchFigure(String path) {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);	
		setBorder(new LineBorder(ColorConstants.black,1));
		setBackgroundColor(classColor);
		setOpaque(true);

		Label label = new Label(path);
		add(label);	
	}
}

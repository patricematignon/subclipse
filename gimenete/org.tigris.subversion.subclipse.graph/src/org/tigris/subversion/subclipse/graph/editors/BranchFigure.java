package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.graphics.Color;

public class BranchFigure extends Figure {
	
	public static Color classColor = new Color(null,255,255,206);
	
	public BranchFigure(String path) {
		setLayoutManager(new BorderLayout());	
		setBorder(new LineBorder(ColorConstants.black,1));
		setBackgroundColor(classColor);
		setOpaque(true);

		Label label = new Label(path);
		add(label, BorderLayout.CENTER);
		
		setToolTip(new Label(path));
	}
}

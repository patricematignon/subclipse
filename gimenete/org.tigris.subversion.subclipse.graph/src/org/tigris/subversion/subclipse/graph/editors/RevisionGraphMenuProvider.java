package org.tigris.subversion.subclipse.graph.editors;

import java.util.Iterator;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.IMenuManager;

public class RevisionGraphMenuProvider extends ContextMenuProvider {

	public RevisionGraphMenuProvider(EditPartViewer viewer) {
		super(viewer);
	}

	public void buildContextMenu(IMenuManager menu) {
		GraphEditPart graphEditPart = (GraphEditPart)getViewer().getContents();
		NodeFigure nodeFigure = graphEditPart.getSelectedNode();
		if (nodeFigure != null) {
			
		}
		Iterator iter = getViewer().getSelectedEditParts().iterator();
		while (iter.hasNext()) {
			EditPart editPart = (EditPart)iter.next();
			System.out.println("editPart: " + editPart.getClass().getName());
		}
	}

}

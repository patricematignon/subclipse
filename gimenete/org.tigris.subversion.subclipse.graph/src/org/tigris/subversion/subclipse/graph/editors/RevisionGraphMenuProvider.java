package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.IMenuManager;
import org.tigris.subversion.subclipse.graph.popup.actions.SetCommitPropertiesAction;

public class RevisionGraphMenuProvider extends ContextMenuProvider {
	private RevisionGraphEditor editor;

	public RevisionGraphMenuProvider(EditPartViewer viewer, RevisionGraphEditor editor) {
		super(viewer);
		this.editor = editor;
	}

	public void buildContextMenu(IMenuManager menu) {
		GraphEditPart graphEditPart = (GraphEditPart)getViewer().getContents();
		NodeFigure nodeFigure = graphEditPart.getSelectedNode();
		if (nodeFigure != null) {
			menu.add(new SetCommitPropertiesAction(nodeFigure, editor));
		}
//		Iterator iter = getViewer().getSelectedEditParts().iterator();
//		while (iter.hasNext()) {
//			EditPart editPart = (EditPart)iter.next();
//			System.out.println("editPart: " + editPart.getClass().getName());
//		}
	}

}

package org.tigris.subversion.subclipse.graph.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.tigris.subversion.subclipse.graph.cache.Node;
import org.tigris.subversion.subclipse.graph.popup.actions.BranchTagAction;
import org.tigris.subversion.subclipse.graph.popup.actions.CompareRevisionsAction;
import org.tigris.subversion.subclipse.graph.popup.actions.ImageAction;
import org.tigris.subversion.subclipse.graph.popup.actions.RefreshNodeAction;
import org.tigris.subversion.subclipse.graph.popup.actions.RevisionDetailsAction;
import org.tigris.subversion.subclipse.graph.popup.actions.SetCommitPropertiesAction;

public class RevisionGraphMenuProvider extends ContextMenuProvider {
	private RevisionGraphEditor editor;

	public RevisionGraphMenuProvider(EditPartViewer viewer, RevisionGraphEditor editor) {
		super(viewer);
		this.editor = editor;
	}

	public void buildContextMenu(IMenuManager menu) {
		List selectedRevisions = getSelectedRevisions();
		if (selectedRevisions.size() == 1) {
			RevisionEditPart revision = (RevisionEditPart)selectedRevisions.get(0);
			Node node = (Node)revision.getModel();
			NodeFigure nodeFigure = (NodeFigure)revision.getFigure();
			menu.add(new RevisionDetailsAction(node, editor));
			menu.add(new SetCommitPropertiesAction(nodeFigure, editor));
			menu.add(new BranchTagAction("Create Branch/Tag from Revision " + node.getRevision() + "...", editor, node));
		}
		if (selectedRevisions.size() > 0) {
			menu.add(new RefreshNodeAction(selectedRevisions, editor));	
		}
		menu.add(new Separator());
		if (selectedRevisions.size() == 2) {
			RevisionEditPart revision1 = (RevisionEditPart)selectedRevisions.get(0);
			Node node1 = (Node)revision1.getModel();
			RevisionEditPart revision2 = (RevisionEditPart)selectedRevisions.get(1);
			Node node2 = (Node)revision2.getModel();
			menu.add(new CompareRevisionsAction(node1, node2, editor));
		}
		menu.add(new Separator());
		menu.add(new ImageAction(editor));
	}
	
	private List getSelectedRevisions() {
		List selectedRevisions = new ArrayList();
		List selectedEditParts = getViewer().getSelectedEditParts();
		Iterator iter = selectedEditParts.iterator();
		while (iter.hasNext()) {
			EditPart editPart = (EditPart)iter.next();
			if (editPart instanceof RevisionEditPart) selectedRevisions.add(editPart);
		}
		return selectedRevisions;
	}

}

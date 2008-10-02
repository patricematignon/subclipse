package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

public class GraphActionBarContributor extends ActionBarContributor {
	private RevisionGraphEditor editor;

	public void setActiveEditor(IEditorPart editor) {
		super.setActiveEditor(editor);
		this.editor = (RevisionGraphEditor)editor;
	}

	protected void buildActions() {
	}

	protected void declareGlobalActionKeys() {
	}
	
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(new Separator());
        toolBarManager.add(new ZoomComboContributionItem(getPage()));
        toolBarManager.add(new Separator());
        Action refreshAction = new Action() {
			public void run() {
				editor.refresh();
			}        	
        };
        refreshAction.setImageDescriptor(SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_REFRESH));
        toolBarManager.add(refreshAction);
	}
	
	/*
	public void contributeToMenu(IMenuManager menuManager) {
		super.contributeToMenu(menuManager);
		MenuManager viewMenu = new MenuManager("View");
		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
	}
	*/

}

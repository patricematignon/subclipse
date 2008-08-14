package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

public class GraphActionBarContributor extends ActionBarContributor {

	protected void buildActions() {
	}

	protected void declareGlobalActionKeys() {
	}
	
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(new Separator());
        toolBarManager.add(new ZoomComboContributionItem(getPage()));
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

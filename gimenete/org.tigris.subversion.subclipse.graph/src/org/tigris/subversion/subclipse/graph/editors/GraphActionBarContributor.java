package org.tigris.subversion.subclipse.graph.editors;

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.tigris.subversion.subclipse.graph.Activator;
import org.tigris.subversion.subclipse.graph.popup.actions.ImageAction;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

public class GraphActionBarContributor extends ActionBarContributor {
	private RevisionGraphEditor editor;
	private static ToggleShowDeletedAction[] toggleShowDeletedActions;

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
        
        toggleShowDeletedActions = new ToggleShowDeletedAction[] {
        	new ToggleShowDeletedAction("If modified", RevisionGraphEditor.SHOW_DELETED_MODIFIED),
        	new ToggleShowDeletedAction("Yes", RevisionGraphEditor.SHOW_DELETED_YES),
        	new ToggleShowDeletedAction("No", RevisionGraphEditor.SHOW_DELETED_NO)
        };
        ShowDeletedAction showDeletedAction = new ShowDeletedAction();
        toolBarManager.add(showDeletedAction);
        
        Action imageAction = new Action() {
			public void run() {
				Action action = new ImageAction(editor);
				action.run();
			}            	
        };
        imageAction.setImageDescriptor(SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_EXPORT_IMAGE));
        toolBarManager.add(imageAction);
	}
	
	/*
	public void contributeToMenu(IMenuManager menuManager) {
		super.contributeToMenu(menuManager);
		MenuManager viewMenu = new MenuManager("View");
		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
	}
	*/
	
	public static class ShowDeletedAction extends Action implements IMenuCreator {
		private Menu menu;
		
		public ShowDeletedAction() {
			setText("Show deleted branches");
			setImageDescriptor(SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_SHOW_DELETED));			
			setMenuCreator(this);
		}
		
		public void dispose() {
			if (menu != null)  {
				menu.dispose();
				menu= null;
			}
		}
		
		public Menu getMenu(Control parent) {
			if (menu != null) menu.dispose();
			menu = new Menu(parent);
			addActionToMenu(menu, toggleShowDeletedActions[0]);
			addActionToMenu(menu, toggleShowDeletedActions[1]);
			addActionToMenu(menu, toggleShowDeletedActions[2]);
			return menu;
		}
		
		public Menu getMenu(Menu parent) {
			return null;
		}
		
		private void addActionToMenu(Menu parent, Action action) {
			ActionContributionItem item= new ActionContributionItem(action);
			item.fill(parent, -1);			
		}
		
	}
	
	public class ToggleShowDeletedAction extends Action {
		private final int show;

		public ToggleShowDeletedAction(String text, int show) {
			super(text, AS_RADIO_BUTTON);
			this.show = show;
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			setChecked(show == store.getInt(RevisionGraphEditor.SHOW_DELETED_PREFERENCE));
		}
		
	    public int getShow() {
	        return show;
	    }	
	    
	    public void run() {
	    	if (isChecked()) {
	    		Activator.getDefault().getPreferenceStore().setValue(RevisionGraphEditor.SHOW_DELETED_PREFERENCE, show);
	    		editor.refresh();
	    	}
	    }
		
	}

}

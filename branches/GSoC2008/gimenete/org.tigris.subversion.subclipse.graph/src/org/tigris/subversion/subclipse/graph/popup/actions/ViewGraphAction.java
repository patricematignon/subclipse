package org.tigris.subversion.subclipse.graph.popup.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;
import org.tigris.subversion.subclipse.graph.editors.RevisionGraphEditorInput;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.actions.SVNAction;

public class ViewGraphAction extends SVNAction {

	/*
	 * @see SVNAction#executeIAction)
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IResource[] resources = getSelectedResources();
//				RevisionGraphEditor editor;
				try {
					if (resources.length > 0) {
						IEditorPart part = getTargetPage().openEditor(
								new RevisionGraphEditorInput(resources[0]),
								"org.tigris.subversion.subclipse.graph.editors.revisionGraphEditor");
//						editor = (RevisionGraphEditor) part;
//						if(editor != null) {
//							editor.showGraphFor(resources[0]);
//						}
					}
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		return true;
//		ISVNRemoteResource[] resources = getSelectedRemoteResources();
//		if (resources.length == 1) return true;
//		if (resources.length == 0 && getSelectedRemoteFolders().length == 1) return true;
//		return false;
	}
	/**
	 * @see org.tigris.subversion.subclipse.ui.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("ShowHistoryAction.showHistory"); //$NON-NLS-1$
	}

	/*
	 * @see org.tigris.subversion.subclipse.ui.actions.ReplaceableIconAction#getImageId()
	 */
	protected String getImageId() {
		return ISVNUIConstants.IMG_MENU_SHOWHISTORY;
	}
}
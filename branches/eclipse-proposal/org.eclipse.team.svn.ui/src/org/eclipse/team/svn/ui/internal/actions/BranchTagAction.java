package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.dialogs.BranchTagDialog;
import org.eclipse.team.svn.ui.internal.operations.BranchTagOperation;

public class BranchTagAction extends WorkspaceAction {

    protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
        IResource[] resources = getSelectedResources();
        for (int i = 0; i < resources.length; i++) {
            BranchTagDialog dialog = new BranchTagDialog(getShell(), resources[i]);
            if (dialog.open() == BranchTagDialog.CANCEL) break;
            SVNUrl sourceUrl = dialog.getUrl();
            SVNUrl destinationUrl = dialog.getToUrl();
            String message = dialog.getComment();
            boolean createOnServer = dialog.isCreateOnServer();
            BranchTagOperation branchTagOperation = new BranchTagOperation(getTargetPart(), getSelectedResources(), sourceUrl, destinationUrl, createOnServer, dialog.getRevision(), message);
            branchTagOperation.setNewAlias(dialog.getNewAlias());
            branchTagOperation.run();
        }          
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("BranchTagAction.branch"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForManagedResources()
	 */
	protected boolean isEnabledForManagedResources() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForMultipleResources()
	 */
	protected boolean isEnabledForMultipleResources() {
		return false;
	}	       
    

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
    protected boolean isEnabledForAddedResources() {
        return false;
    }
}

package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.dialogs.SwitchDialog;
import org.eclipse.team.svn.ui.internal.operations.SwitchOperation;

/**
 * Action to switch to branch/tag 
 */
public class SwitchAction extends WorkspaceAction {

    protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
        IResource[] resources = getSelectedResources(); 
        for (int i = 0; i < resources.length; i++) {
            SwitchDialog dialog = new SwitchDialog(getShell(), resources[i]);
            if (dialog.open() == SwitchDialog.CANCEL) break;
            SVNUrl svnUrl = dialog.getUrl();
            SVNRevision svnRevision = dialog.getRevision();
            new SwitchOperation(getTargetPart(), getSelectedResources(), svnUrl, svnRevision).run();
        }
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("SwitchAction.switch"); //$NON-NLS-1$
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

}

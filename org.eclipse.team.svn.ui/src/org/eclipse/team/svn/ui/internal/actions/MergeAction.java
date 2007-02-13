package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.dialogs.MergeDialog;
import org.eclipse.team.svn.ui.internal.operations.MergeOperation;

public class MergeAction extends WorkspaceAction {

    protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
        IResource[] resources = getSelectedResources(); 
        for (int i = 0; i < resources.length; i++) {
            MergeDialog dialog = new MergeDialog(getShell(), resources[i]);
            if (dialog.open() == MergeDialog.CANCEL) break;
            SVNUrl svnUrl1 = dialog.getFromUrl();
            SVNRevision svnRevision1 = dialog.getFromRevision();
            SVNUrl svnUrl2 = dialog.getToUrl();
            SVNRevision svnRevision2 = dialog.getToRevision();            
            new MergeOperation(getTargetPart(), getSelectedResources(), svnUrl1, svnRevision1, svnUrl2, svnRevision2).run();      
        }   
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("MergeAction.merge"); //$NON-NLS-1$
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

/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal.actions;
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.dialogs.IgnoreResourcesDialog;
import org.eclipse.team.svn.ui.internal.operations.IgnoreOperation;

public class IgnoreAction extends WorkspaceAction {
	
	protected void execute(final IAction action) throws InvocationTargetException, InterruptedException {
        run(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IResource[] resources = getSelectedResources();
                IgnoreResourcesDialog dialog = new IgnoreResourcesDialog(getShell(), resources);
                if (dialog.open() != IgnoreResourcesDialog.OK) return;
                new IgnoreOperation(getTargetPart(), resources, dialog).run();
                
                //if (action != null) action.setEnabled(isEnabled());
            }
        }, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
    
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("IgnoreAction.ignore"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.WorkspaceAction#isEnabledForSVNResource(org.eclipse.team.svn.core.internal.ISVNLocalResource)
	 */
	protected boolean isEnabledForSVNResource(ISVNLocalResource svnResource) throws SVNException {
	    //If the resource is a IProject then the action should not be enabled.
	    if( svnResource.getIResource() instanceof IProject)
	        return false;
		// If the parent is not managed there is no way to set the svn:ignore property
		if (!svnResource.getParent().isManaged()) {
			return false;
		}
		return super.isEnabledForSVNResource(svnResource);
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForManagedResources()
	 */
	protected boolean isEnabledForManagedResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return true;
	}
	
}
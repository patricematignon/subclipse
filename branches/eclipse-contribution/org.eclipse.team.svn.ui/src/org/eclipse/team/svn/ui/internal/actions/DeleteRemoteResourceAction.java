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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.svn.ui.internal.repository.RepositoryManager;

/**
 * Action to delete a remote resource on repository
 */
public class DeleteRemoteResourceAction extends SVNAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action)
		throws InvocationTargetException, InterruptedException {
        RepositoryManager manager = SVNUIPlugin.getPlugin().getRepositoryManager();
        final String message = manager.promptForComment(getShell(), new IResource[]{});
        
        if (message == null)
            return; // cancel
        
        run(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    SVNProviderPlugin.getPlugin().getRepositoryResourcesManager().
                        deleteRemoteResources(        
                            getSelectedRemoteResources(),message,monitor);
                } catch (TeamException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }, true /* cancelable */, PROGRESS_BUSYCURSOR); //$NON-NLS-1$        

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		return getSelectedRemoteResources().length > 0;
	}

}
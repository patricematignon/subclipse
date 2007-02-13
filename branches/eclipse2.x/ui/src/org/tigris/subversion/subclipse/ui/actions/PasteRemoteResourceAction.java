/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion  
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.RemoteResourceTransfer;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.repository.RepositoryManager;

/**
 * Paste remote resources to selected directory 
 */
public class PasteRemoteResourceAction extends SVNAction {

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.ui.actions.SVNAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action)
		throws InvocationTargetException, InterruptedException {
            RepositoryManager manager = SVNUIPlugin.getPlugin().getRepositoryManager();
            final String message = manager.promptForComment(getShell(), new IResource[]{});

            if (message == null)
                return; // canceled

            Clipboard clipboard = new Clipboard(getShell().getDisplay());
            final ISVNRemoteResource resource = (ISVNRemoteResource)clipboard.getContents(RemoteResourceTransfer.getInstance());
            clipboard.dispose();
            
            ISVNRemoteResource selectedResource = getSelectedRemoteResources()[0];
            final ISVNRemoteFolder destination = 
                (selectedResource.isFolder()?
                    (ISVNRemoteFolder)selectedResource:selectedResource.getParent());
            
            run(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
                    try {
                        SVNProviderPlugin.getPlugin().getRepositoryResourcesManager().
                            copyRemoteResource(resource,destination,message,monitor);
                    } catch (TeamException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            }, true /* cancelable */, PROGRESS_BUSYCURSOR); //$NON-NLS-1$
    }

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
        if (getSelectedRemoteResources().length != 1)
            return false;
        
        boolean result;
        Clipboard clipboard = new Clipboard(getShell().getDisplay());
        result = clipboard.getContents(RemoteResourceTransfer.getInstance()) != null;
        clipboard.dispose();
		return result;
	}

}

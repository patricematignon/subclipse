/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Action to recursively cleanup any locks in teh working copy
 */
public class CleanupAction extends WorkspaceAction {

    protected void execute(final IAction action)
            throws InvocationTargetException, InterruptedException {
        run(new WorkspaceModifyOperation() {
            public void execute(IProgressMonitor monitor)
                    throws InvocationTargetException {
                IResource[] resources = getSelectedResources();

                try {
                    for (int i = 0; i < resources.length; i++) {

                        ISVNLocalResource svnResource = SVNWorkspaceRoot
                                .getSVNResourceFor(resources[i]);
                        svnResource.getRepository().getSVNClient().cleanup(svnResource.getFile());

                        resources[i].refreshLocal(IResource.DEPTH_INFINITE,
                                monitor);
                    }
                    // fix the action enablement
                    if (action != null)
                        action.setEnabled(isEnabled());
                } catch (TeamException e) {
                    throw new InvocationTargetException(e);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } catch (SVNClientException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }, false /* cancelable */, PROGRESS_BUSYCURSOR);
    }

    /**
     * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
     */
    protected String getErrorTitle() {
        return Policy.bind("CleanupAction.cleanup"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.ui.actions.WorkspaceAction#isEnabledForSVNResource(org.tigris.subversion.subclipse.core.ISVNLocalResource)
     */
    protected boolean isEnabledForSVNResource(ISVNLocalResource svnResource) throws SVNException {
        return svnResource.isFolder() && super.isEnabledForSVNResource(svnResource);
    }
    
    /**
     * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
     */
    protected boolean isEnabledForAddedResources() {
        return false;
    }

}

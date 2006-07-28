/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.core.internal.commands;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;

/**
 * Remove stale transactions and locks from the working copy.  This operation can only be run
 * on folders.
 */
public class CleanupResourcesCommand implements ISVNCommand {

    private final SVNWorkspaceRoot root;
    private final IResource[] resources;

    public CleanupResourcesCommand(SVNWorkspaceRoot root, IResource[] resources) {
        this.root = root;
        this.resources = resources;
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws SVNException {
        try {
            monitor.beginTask(null, 100 * resources.length);
            ISVNClientAdapter svnClient = root.getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient);
            
            for (int i = 0; i < resources.length; i++) {
                svnClient.cleanup(resources[i].getLocation().toFile());
                monitor.worked(100);
            }
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            OperationManager.getInstance().endOperation();
            monitor.done();
        }
    } 
}

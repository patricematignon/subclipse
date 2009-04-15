/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.eclipse.team.svn.core.internal.commands;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;

/**
 * Unlock resources
 * 
 */
public class UnlockResourcesCommand implements ISVNCommand {
	// resources to unlock
    private IResource[] resources;
    // break the lock of another owner
    private boolean force;
    
    private SVNWorkspaceRoot root;

    public UnlockResourcesCommand(SVNWorkspaceRoot root, IResource[] resources, boolean force) {
    	this.resources = resources;
        this.force = force;
        this.root = root;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {
        final ISVNClientAdapter svnClient = root.getRepository().getSVNClient();
        
        final File[] resourceFiles = new File[resources.length];
        for (int i = 0; i < resources.length;i++)
            resourceFiles[i] = resources[i].getLocation().toFile(); 
        
        try {
            monitor.beginTask(null, 100);
            OperationManager.getInstance().beginOperation(svnClient);

            svnClient.unlock(resourceFiles, force);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            OperationManager.getInstance().endOperation();
            monitor.done();
        }
 	}
    
}
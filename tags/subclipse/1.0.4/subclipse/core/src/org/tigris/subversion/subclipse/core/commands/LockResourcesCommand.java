/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.commands;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.client.OperationManager;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Lock resources
 * 
 */
public class LockResourcesCommand implements ISVNCommand {
	// resources to lock
    private IResource[] resources;
    // lock comment -- may be null
    private String message;
    // steal the lock from other owner
    private boolean force;
    
    private SVNWorkspaceRoot root;

    public LockResourcesCommand(SVNWorkspaceRoot root, IResource[] resources, boolean force, String message) {
    	this.resources = resources;
        this.message = message;
        this.force = force;
        this.root = root;
    }
    
	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.core.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {
        final ISVNClientAdapter svnClient = root.getRepository().getSVNClient();
        
        final File[] resourceFiles = new File[resources.length];
        for (int i = 0; i < resources.length;i++)
            resourceFiles[i] = resources[i].getLocation().toFile(); 
        try {
            monitor.beginTask(null, 100);
            OperationManager.getInstance().beginOperation(svnClient);

            svnClient.lock(resourceFiles,message,force);
            // Ensure resource is writable.  SVN will only change read-only
            // flag if the svn:needs-lock property is set.
            for (int i = 0; i < resources.length; i++) {
				makeWritable(resources[i]);
			}
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            OperationManager.getInstance().endOperation();
            monitor.done();
        }
	}
	
	private void makeWritable(IResource resource) {
        if (resource.getType() == IResource.FILE && resource.isReadOnly()) {
    	    resource.setReadOnly(false);
        }
	}
    
}
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
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.client.OperationProgressNotifyListener;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;

/**
 * Update the given resources in the given project to the given revision
 * 
 * @author Cedric Chabanois (cchab at tigris.org)
 */
public class UpdateResourcesCommand implements ISVNCommand {
    private SVNWorkspaceRoot root;
    private IResource[] resources;
    private SVNRevision revision;
    private boolean recursive;    
    
    /**
     * Update the given resources.
     * BEWARE ! The resource array has to be sorted properly, so parent folder (incoming additions) are updated sooner than their children.
     * BEWARE ! For incoming deletions, it has to be opposite. 
     * WATCH OUT ! These two statements mean that you CANNOT have both additions and deletions within the same call !!!
     * When doing recursive call, it's obviously not an issue ... 
     * @param root
     * @param resources
     * @param revision
     * @param recursive
     */
    public UpdateResourcesCommand(SVNWorkspaceRoot root, IResource[] resources, SVNRevision revision, boolean recursive) {
        this.root = root;
        this.resources = resources;
        this.revision = revision;
        this.recursive = recursive;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(final IProgressMonitor monitor) throws SVNException {
        try {
            monitor.beginTask(null, 100 * resources.length);                    
            ISVNClientAdapter svnClient = root.getRepository().getSVNClient();

            OperationManager.getInstance().beginOperation(svnClient, new OperationProgressNotifyListener(monitor));
    		if (resources.length == 1)
    		{
                monitor.subTask(resources[0].getName());
                svnClient.update(resources[0].getLocation().toFile(),revision, recursive);
                monitor.worked(100);    			
    		}
    		else
    		{
    			File[] files = new File[resources.length];
    			for (int i = 0; i < resources.length; i++) {
					files[i] = resources[i].getLocation().toFile();
				}
   				svnClient.update(files, revision, recursive, false);
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
/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.core.internal.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.ISVNRunnable;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.client.OperationProgressNotifyListener;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;

/**
 * Checkin any local changes to given resources in the given project
 * 
 * @author Cedric Chabanois (cchab at tigris.org) 
 */
public class CheckinResourcesCommand implements ISVNCommand {
	// resources to commit
	protected IResource[] resources;
    
    protected String message;
    
    protected boolean keepLocks;
    
    protected int depth;
    
    protected SVNWorkspaceRoot root;

    public CheckinResourcesCommand(SVNWorkspaceRoot root, IResource[] resources, int depth, String message, boolean keepLocks) {
    	this.resources = resources;
        this.message = message;
        this.depth = depth;
        this.root = root;
        this.keepLocks = keepLocks;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {
        final ISVNClientAdapter svnClient = root.getRepository().getSVNClient();
        
        // Prepare the parents list
        // we will Auto-commit parents if they are not already commited
        List parentsList = new ArrayList();
        for (int i=0; i<resources.length; i++) {
            IResource currentResource = resources[i];
            IContainer parent = currentResource.getParent();
            ISVNLocalResource svnParentResource = SVNWorkspaceRoot.getSVNResourceFor(parent);
            while (parent.getType() != IResource.ROOT && 
                   parent.getType() != IResource.PROJECT && 
                   !svnParentResource.hasRemote()) {
            	if (!inCommitList(parent))
            		parentsList.add(parent);
                parent = parent.getParent();
                svnParentResource = svnParentResource.getParent();
            }
        }
        
        // convert parents and resources to an array of File
        final File[] parents = new File[parentsList.size()];
        for (int i = 0; i < parentsList.size();i++)
            parents[i] = ((IResource)parentsList.get(i)).getLocation().toFile();
            
        final File[] resourceFiles = new File[resources.length];
        for (int i = 0; i < resources.length;i++)
            resourceFiles[i] = resources[i].getLocation().toFile(); 
        
        SVNProviderPlugin.run(new ISVNRunnable() {
            public void run(final IProgressMonitor pm) throws SVNException {
                try {
                    pm.beginTask(null, resourceFiles.length);
                    OperationManager.getInstance().beginOperation(svnClient, new OperationProgressNotifyListener(pm));
                    
                    // we commit the parents (not recursively)
                    if (parents.length > 0) {
                    	if (svnClient.canCommitAcrossWC()) svnClient.commitAcrossWC(parents,message,false,false,true);
                    	else svnClient.commit(parents,message,false,false);
                    }
                    
                    // then the resources the user has requested to commit
                    if (svnClient.canCommitAcrossWC()) svnClient.commitAcrossWC(resourceFiles,message,depth == IResource.DEPTH_INFINITE,keepLocks,true);
                    else svnClient.commit(resourceFiles,message,depth == IResource.DEPTH_INFINITE,keepLocks);
                } catch (SVNClientException e) {
                    throw SVNException.wrapException(e);
                } finally {
                    OperationManager.getInstance().endOperation();
                    pm.done();
                }
            }
        }, Policy.monitorFor(monitor));
	}
    
	private boolean inCommitList(IResource resource) {
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].equals(resource))
				return true;
		}
		return false;
	}
}
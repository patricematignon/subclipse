/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import org.eclipse.core.internal.resources.IManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;

/**
 * Provides a method to get the status of a resource. <br>   
 * It is much more efficient to get the status of a set a resources than only
 * one resource at a time.<br>
 * So this class asks the status of the given resource but also of all its children. <br>
 * We use a tree to keep the status of the resources  
 * 
 */
public class StatusCacheManager implements IManager{
    private StatusCacheComposite treeCacheRoot = new StatusCacheComposite();
    
    
    public StatusCacheManager() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.internal.resources.IManager#startup(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void startup(IProgressMonitor monitor) throws CoreException {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.internal.resources.IManager#shutdown(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void shutdown(IProgressMonitor monitor) throws CoreException {
    }
    
    /**
     * update the status of <code>resource</code> and all of its child resources
     * @param resource Resource to update status of
     * @throws SVNException
     */
    private void recursiveUpdateStatusSet(IResource resource) throws SVNException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot workspaceRoot = workspace.getRoot();

        if (!(resource instanceof IProject)) {
            // if the status of the resource parent is not known, we
            // recursively update it instead 
            IContainer parent = resource.getParent();
            if (parent != null) {
                if (treeCacheRoot.getStatus(parent) == null) {
                    recursiveUpdateStatusSet(parent);
                    return;
                }
            }
        }
        
        
        if (Policy.DEBUG_STATUS) {
            System.out.println("[svn] getting status for : " + resource.getFullPath()); //$NON-NLS-1$   
        }
        
        // don't do getRepository().getSVNClient() as we can ask the status of a file
        // that is not associated with a known repository
        // we don't need login & password so this is not a problem
        ISVNStatus[] statuses = null;
        try {
            ISVNClientAdapter svnClientAdapterStatus = SVNProviderPlugin.getPlugin().createSVNClient();
            statuses = svnClientAdapterStatus.getStatus(resource.getLocation().toFile(),true, true);
        } catch (SVNClientException e1) {
            throw SVNException.wrapException(e1);
        }
        
        IPath baseResourceLocation = resource.getLocation();
        int segments = baseResourceLocation.segmentCount();
        
        for (int i = 0; i < statuses.length;i++) {
            ISVNStatus status = statuses[i];
            
            IPath pathEclipse = baseResourceLocation.append(new Path(status.getFile().getAbsolutePath()).removeFirstSegments(segments));
            
            IResource resourceStatus = null;
            
            // we can't test using file.isDirectory and file.isFile because both return false when
            // the resource has been deleted
            if (status.getNodeKind().equals(SVNNodeKind.DIR)) {
                resourceStatus = workspaceRoot.getContainerForLocation(pathEclipse);
            }
            else {
                resourceStatus = workspaceRoot.getFileForLocation(pathEclipse);
            }
            
            if (resourceStatus != null) {
            	treeCacheRoot.addStatus(resourceStatus, new LocalResourceStatus(status));
            }
        }
    }
    
    /**
     * get the status of the given resource
     * @throws SVNException
     */
    public LocalResourceStatus getStatus(IResource resource) throws SVNException {
        LocalResourceStatus status = null;

        status = treeCacheRoot.getStatus(resource);
        
        // we get it using svn 
        if (status == null)
        {
            recursiveUpdateStatusSet(resource);
            
            status = treeCacheRoot.getStatus(resource);
        }
        
        if (status == null) {
            status = new LocalResourceStatus(new SVNStatusUnversioned(resource.getLocation().toFile(),false));
            treeCacheRoot.addStatus(resource, status);
        }
        
        
        return status;
    }

    /**
     * refresh the status for the given resource
     * @param resource
     * @param depth
     * @throws SVNException
     */
    public void refreshStatus(IResource resource,int depth) throws SVNException {
        treeCacheRoot.removeStatus(resource,depth);
    }


    
    
}

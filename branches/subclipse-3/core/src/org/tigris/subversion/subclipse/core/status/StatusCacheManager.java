/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.status;

import org.eclipse.core.internal.resources.IManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;

/**
 * Provides a method to get the status of a resource. <br>   
 * It is much more efficient to get the status of a set a resources than only
 * one resource at a time. For that we use a @link org.tigris.subversion.subclipse.core.status.StatusUpdateStrategy<br>
 * 
 * We use a tree (@link org.tigris.subversion.subclipse.core.status.StatusCacheComposite) to keep the status of the resources  
 * 
 * @author cedric chabanois (cchab at tigris.org)
 */
public class StatusCacheManager implements IManager{
    private StatusCacheComposite treeCacheRoot = new StatusCacheComposite();
    private StatusUpdateStrategy statusUpdateStrategy = new NonRecursiveStatusUpdateStrategy();
    
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
     * get the status of the given resource
     * @throws SVNException
     */
    public LocalResourceStatus getStatus(IResource resource) throws SVNException {
        LocalResourceStatus status = null;

        status = treeCacheRoot.getStatus(resource);
        
        // we get it using svn 
        if (status == null)
        {
            statusUpdateStrategy.setTreeCacheRoot(treeCacheRoot);
            statusUpdateStrategy.updateStatus(resource);
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

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;

/**
 * Add the given resources to the project. 
 * <p>
 * The sematics follow that of SVN in the sense that any folders and files
 * are created remotely on the next commit. 
 * </p>
 * 
 * @author Cedric Chabanois (cchab at tigris.org)
 */
public class AddResourcesCommand implements ISVNCommand {
	// resources to add
    private IResource[] resources; 
    
    private int depth;
    
    private SVNWorkspaceRoot root;
    
    public AddResourcesCommand(SVNWorkspaceRoot root, IResource[] resources, int depth) {
        this.resources = resources;
        this.depth = depth;
        this.root = root;
    }
    
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {
        monitor = Policy.monitorFor(monitor);
        
        // Visit the children of the resources using the depth in order to
        // determine which folders, text files and binary files need to be added
        // A TreeSet is needed for the folders so they are in the right order (i.e. parents created before children)
        final SortedSet folders = new TreeSet();
        // Sets are required for the files to ensure that files will not appear twice if there parent was added as well
        // and the depth isn't zero
        final HashSet files = new HashSet();
        
        for (int i=0; i<resources.length; i++) {
            
            final IResource currentResource = resources[i];
            
            try {       
                // Auto-add parents if they are not already managed
                IContainer parent = currentResource.getParent();
                ISVNLocalResource svnParentResource = SVNWorkspaceRoot.getSVNResourceFor(parent);
                while (parent.getType() != IResource.ROOT && parent.getType() != IResource.PROJECT && ! svnParentResource.isManaged()) {
                    folders.add(svnParentResource);
                    parent = parent.getParent();
                    svnParentResource = svnParentResource.getParent();
                }
                    
                // Auto-add children accordingly to depth
                final SVNException[] exception = new SVNException[] { null };
                currentResource.accept(new IResourceVisitor() {
                    public boolean visit(IResource resource) {
                        try {
                            ISVNLocalResource mResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                            // Add the resource is its not already managed and it was either
                            // added explicitly (is equal currentResource) or is not ignored
                            if ((! mResource.isManaged()) && (currentResource.equals(resource) || ! mResource.isIgnored())) {
                                if (resource.getType() == IResource.FILE) {
                                    files.add(mResource);
                                } else {
                                    folders.add(mResource);
                                }
                            }
                            // Always return true and let the depth determine if children are visited
                            return true;
                        } catch (SVNException e) {
                            exception[0] = e;
                            return false;
                        }
                    }
                }, depth, false);
                if (exception[0] != null) {
                    throw exception[0];
                }
            } catch (CoreException e) {
                throw new SVNException(new Status(IStatus.ERROR, SVNProviderPlugin.ID, TeamException.UNABLE, Policy.bind("SVNTeamProvider.visitError", new Object[] {resources[i].getFullPath()}), e)); //$NON-NLS-1$
            }
        } // for
        // If an exception occured during the visit, throw it here

        // Add the folders, followed by files!
        ISVNClientAdapter svnClient = root.getRepository().getSVNClient();
        monitor.beginTask(null, files.size() * 10 + (folders.isEmpty() ? 0 : 10));
        OperationManager.getInstance().beginOperation(svnClient);
        try {
            for(Iterator it=folders.iterator(); it.hasNext();) {
                final ISVNLocalResource localResource = (ISVNLocalResource)it.next();
  
                try {
                    svnClient.addDirectory(localResource.getIResource().getLocation().toFile(),false);
                } catch (SVNClientException e) {
                    throw SVNException.wrapException(e);
                }
            }

            for(Iterator it=files.iterator(); it.hasNext();) {
                final ISVNLocalResource localResource = (ISVNLocalResource)it.next();
  
                try {
                    svnClient.addFile(localResource.getIResource().getLocation().toFile());
                    // If file has read-only attribute set, remove it
                    ResourceAttributes attrs = localResource.getIResource().getResourceAttributes();
                    if (localResource.getIResource().getType() == IResource.FILE && attrs.isReadOnly()) {
                        attrs.setReadOnly(false);
                    	try {
							localResource.getIResource().setResourceAttributes(attrs);
						} catch (CoreException swallow) {
						}
                    }
                } catch (SVNClientException e) {
                    throw SVNException.wrapException(e);
                }    
            }
                

        } finally {
            OperationManager.getInstance().endOperation();
            monitor.done();
        }
    }
    
    
}

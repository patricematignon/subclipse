/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.core;
 
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.ResourceRuleFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.tigris.subversion.subclipse.core.client.OperationManager;
import org.tigris.subversion.subclipse.core.resources.RemoteFile;
import org.tigris.subversion.subclipse.core.resources.RemoteFolder;
import org.tigris.subversion.subclipse.core.resources.SVNMoveDeleteHook;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * This class is responsible for configuring a project for repository management
 * and providing the necessary hooks for resource modification
 * This class is created for each project that is associated with a repository provider
 */
public class SVNTeamProvider extends RepositoryProvider {
	private SVNWorkspaceRoot workspaceRoot;
	private Object operations;
	
	/**
	 * Scheduling rule to use when modifying resources.
	 * <code>ResourceRuleFactory</code> only locks the file or its parent if read-only
	 */
	private static final ResourceRuleFactory RESOURCE_RULE_FACTORY = new ResourceRuleFactory() {};
	
	/**
	 * No-arg Constructor for IProjectNature conformance
	 */
	public SVNTeamProvider() {
	}

	/**
	 * @see IProjectNature#deconfigure()
	 */
	public void deconfigure() {

	}
	
    /**
     * @see RepositoryProvider#deconfigured()
     */
	public void deconfigured() {
		SVNProviderPlugin.broadcastProjectDeconfigured(getProject());
	}


	/**
	 * @see IProjectNature#setProject(IProject)
	 */
	public void setProject(IProject project) {
		super.setProject(project);
		try {
			this.workspaceRoot = new SVNWorkspaceRoot(project);
			// Ensure that the project has SVN info
			if (!workspaceRoot.getLocalRoot().hasRemote()) {
				throw new SVNException(new SVNStatus(SVNStatus.ERROR, Policy.bind("SVNTeamProvider.noFolderInfo", project.getName()))); //$NON-NLS-1$
			}
		} catch (SVNException e) {
			// Log any problems creating the CVS managed resource
			SVNProviderPlugin.log(e);
		}
	}

	/**
	 * Add the given resources to the project. 
	 * <p>
	 * The sematics follow that of SVN in the sense that any folders and files
	 * are created remotely on the next commit. 
	 * </p>
	 */
	public void add(IResource[] resources, int depth, IProgressMonitor progress) throws SVNException {	
		
		if (progress == null)
			progress = new NullProgressMonitor();
		
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
        ISVNClientAdapter svnClient = getSVNWorkspaceRoot().getRepository().getSVNClient();
		progress.beginTask(null, files.size() * 10 + (folders.isEmpty() ? 0 : 10));
        OperationManager.getInstance().beginOperation(svnClient);
		try {
            for(Iterator it=folders.iterator(); it.hasNext();) {
                final ISVNLocalResource localResource = (ISVNLocalResource)it.next();
  
   				try {
					svnClient.addDirectory(localResource.getIResource().getLocation().toFile(),false);
                    localResource.refreshStatus();
				} catch (SVNClientException e) {
                    throw SVNException.wrapException(e);
                }
            }

            for(Iterator it=files.iterator(); it.hasNext();) {
                final ISVNLocalResource localResource = (ISVNLocalResource)it.next();
  
                try {
                    svnClient.addFile(localResource.getIResource().getLocation().toFile());
                    localResource.refreshStatus();
                } catch (SVNClientException e) {
                    throw SVNException.wrapException(e);
                }    
            }
                

		} finally {
            OperationManager.getInstance().endOperation();
            progress.done();
		}
	}

	/**
	 * Checkin any local changes to given resources
	 * 
	 */
	public void checkin(IResource[] resources, final String comment, final int depth, IProgressMonitor progress) throws TeamException {
		final ISVNClientAdapter svnClient = getSVNWorkspaceRoot().getRepository().getSVNClient();
        
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
            public void run(IProgressMonitor monitor) throws SVNException {
                try {
                    monitor.beginTask(null, 100);
                    OperationManager.getInstance().beginOperation(svnClient);
                    
                    // we commit the parents (not recursively)
                    if (parents.length > 0)
                        svnClient.commit(parents,comment,false);
                    
                    // then the resources the user has requested to commit
                    svnClient.commit(resourceFiles,comment,depth == IResource.DEPTH_INFINITE);
                } catch (SVNClientException e) {
                    throw SVNException.wrapException(e);
                } finally {
                    OperationManager.getInstance().endOperation();
                    monitor.done();
                }
            }
        }, Policy.monitorFor(progress));
	}

    /**
     * Update to given revision
     */
    public void update(final IResource[] resources, final SVNRevision revision, IProgressMonitor progress) throws TeamException {
    
        SVNProviderPlugin.run(new ISVNRunnable() {
            public void run(IProgressMonitor monitor) throws SVNException {
                try {
                    monitor.beginTask(null, 100);                    
                    ISVNClientAdapter svnClient = getSVNWorkspaceRoot().getRepository().getSVNClient();
                    OperationManager.getInstance().beginOperation(svnClient);
                    for (int i = 0; i < resources.length;i++)
                        svnClient.update(resources[i].getLocation().toFile(),revision,true);
                } catch (SVNClientException e) {
                    throw SVNException.wrapException(e);
                } finally {
                    OperationManager.getInstance().endOperation();
                    monitor.done();
                }        

            }
        }, Policy.monitorFor(progress));
    }

    /**
     * update to HEAD revision
     */
    public void update(final IResource[] resources, IProgressMonitor progress) throws TeamException {
        update(resources, SVNRevision.HEAD, progress);
    }


    public SVNWorkspaceRoot getSVNWorkspaceRoot() {
        return workspaceRoot;
    }

    public void configureProject() {
        SVNProviderPlugin.broadcastProjectConfigured(getProject());
    }
    /*
     * @see RepositoryProvider#getID()
     */
    public String getID() {
        return SVNProviderPlugin.getTypeId();
    }

    /**
     * Adds a pattern to the set of ignores for the specified folder.
     * 
     * @param folder the folder
     * @param pattern the pattern
     */
    public void addIgnored(ISVNLocalFolder folder, String pattern) throws SVNException {
        if (!folder.getStatus().isManaged())
            throw new SVNException(IStatus.ERROR, SVNException.UNABLE,
                Policy.bind("SVNTeamProvider.ErrorSettingIgnorePattern", folder.getIResource().getFullPath().toString())); //$NON-NLS-1$
        ISVNClientAdapter svnClient = getSVNWorkspaceRoot().getRepository().getSVNClient();
        try {
            OperationManager.getInstance().beginOperation(svnClient);
            
            try {
                svnClient.addToIgnoredPatterns(folder.getFile(), pattern);
                
                
                // broadcast changes to unmanaged children - they are the only candidates for being ignored
                ISVNResource[] members = folder.members(null, ISVNFolder.UNMANAGED_MEMBERS);
                IResource[] possiblesIgnores = new IResource[members.length];
                for (int i = 0; i < members.length;i++)
                    possiblesIgnores[i] = ((ISVNLocalResource)members[i]).getIResource(); 
                folder.refreshStatus(IResource.DEPTH_ONE);
                SVNProviderPlugin.broadcastSyncInfoChanges(possiblesIgnores);
            }
            catch (SVNClientException e) {
                throw SVNException.wrapException(e);
            }

        } finally {
            OperationManager.getInstance().endOperation();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.team.core.RepositoryProvider#getMoveDeleteHook()
     */
	public IMoveDeleteHook getMoveDeleteHook() {
		return new SVNMoveDeleteHook();
	}

	public IResourceVariant getResourceVariant(IResource resource) throws SVNException{
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(resource);
		return local.getLatestRemoteResource();
        
		
	}

	/**
	 * Create the resource variant for the given local resource from the 
	 * given bytes. The bytes are those that were previously returned
	 * from a call to <code>IResourceVariant#asBytes()</code>.  This means it's already been fetched,
	 * so we should be able to create enough nfo about it to rebuild it to a minimally useable form for
	 * synchronization.
	 * @param resource the local resource
	 * @param bytes the bytes that identify a variant of the resource
	 * @return the resouce variant handle recreated from the bytes
	 * @throws TeamException
	 */
	public IResourceVariant getResourceVariant(IResource resource, byte[] bytes) throws TeamException {
		
		//in this case, asBytes() will return the revision string, so we create 
		//the variant resource with this minimal info.
		
		if(bytes==null)return null;
		if(resource.getType()==IResource.FILE){
			return new RemoteFile(resource, bytes);
		}else if(resource.getType()==IResource.FOLDER || resource.getType()==IResource.PROJECT){
			return new RemoteFolder(resource, bytes);
		}else{
			return null;
		}
		
		

		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.RepositoryProvider#getRuleFactory()
	 */
	public IResourceRuleFactory getRuleFactory() {
		return RESOURCE_RULE_FACTORY;
	}

}

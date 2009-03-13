/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.core.internal.resources;


import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNProperty;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.svn.core.internal.ISVNLocalFolder;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.commands.AddIgnoredPatternCommand;
import org.eclipse.team.svn.core.internal.commands.GetRemoteResourceCommand;
import org.eclipse.team.svn.core.internal.util.Assert;

/**
 * Represents handles to SVN resource on the local file system. Synchronization
 * information is taken from the .svn subdirectories. 
 * 
 * We implement Comparable so that resources are in the right order (i.e. parents created before children)
 * This is used in SVNTeamProvider.add for example.
 * 
 * @see LocalFolder
 * @see LocalFile
 */
public abstract class LocalResource implements ISVNLocalResource, Comparable {

	/** The local resource represented by this handle */
	protected IResource resource;
	
	/**
	 * Creates a SVN handle to the provided resource
	 * @param resource
	 */
	protected LocalResource(IResource resource) {
		Assert.isNotNull(resource);
		this.resource = resource;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#exists()
	 */
	public boolean exists() {
		return resource.exists();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getParent()
	 */
	public ISVNLocalFolder getParent() {
		IContainer parent = resource.getParent();
		if (parent==null) {
			return null;
		}
		return new LocalFolder(parent);
	} 

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNResource#getName()
	 */
	public String getName() {
		return resource.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#isIgnored()
	 */
	public boolean isIgnored() throws SVNException {
		// If the resource is a derived or linked resource, it is ignored
		if (resource.isDerived() || resource.isLinked()) {
			return true;
		}

		// always ignore .svn folder
		if ((resource.getType() == IResource.FOLDER) && SVNProviderPlugin.getPlugin().isAdminDirectory(getName())) { //$NON-NLS-1$
			return true; 
		}

		if(resource.getType()==IResource.ROOT || resource.getType()==IResource.PROJECT ) {
			return false;
		}
		
		// check the global ignores from Team
		if (Team.isIgnoredHint(resource)) {
			return true;
		}

		LocalResourceStatus status = getStatus();
		
		// a managed resource is never ignored
		if (status.isManaged()) {
			return false;
		}

        // check ignore patterns from the .cvsignore file.
        if (status.isIgnored()) {
            return true;
        }
		
		// check the parent, if the parent is ignored
		// then this resource is ignored also
		ISVNLocalFolder parent = getParent();
		if (parent==null) { return false; }
		if (parent.isIgnored()) { return true; }
		
        return false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#setIgnored()
     */
    public void setIgnored() throws SVNException {
        AddIgnoredPatternCommand command = new AddIgnoredPatternCommand(getParent(), resource.getName());
        command.run(null);
    }
    
	/*
	 * @see ISVNLocalResource#isManaged()
	 */
	public boolean isManaged() throws SVNException {
		return !this.resource.isDerived() && getStatus().isManaged();
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#hasRemote()
     */
    public boolean hasRemote() throws SVNException {
        return !isLinked() && getStatus().hasRemote();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#isLinked()
	 */
	public boolean isLinked() {
		return SVNWorkspaceRoot.isLinkedResource(this.resource);
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getStatus()
     */
    public LocalResourceStatus getStatus() throws SVNException {
    	if (isLinked()) { return LocalResourceStatus.NONE; }
    	LocalResourceStatus aStatus = SVNProviderPlugin.getPlugin().getStatusCacheManager().getStatus(resource);
        return (aStatus != null) ? aStatus : LocalResourceStatus.NONE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getRevision()
     */
    public SVNRevision getRevision() throws SVNException {
    	if (isLinked()) { return null; }
    	return SVNProviderPlugin.getPlugin().getStatusCacheManager().getResourceRevision(this);
    }

	/*
	 * @see Comparable#compareTo(Object)
     * the comparaison is used for example in SVNTeamProvider.add
	 */
	public int compareTo(Object arg0) {
		LocalResource other = (LocalResource)arg0;
        // this way, resources will be in order
		return resource.getFullPath().toString().compareTo(other.resource.getFullPath().toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getIResource()
	 */
	public IResource getIResource() {
		return resource;
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getFile()
     */
    public File getFile() {
        return resource.getLocation().toFile();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getWorkspaceRoot()
	 */
	public SVNWorkspaceRoot getWorkspaceRoot() {
		SVNTeamProvider teamProvider = (SVNTeamProvider)RepositoryProvider.getProvider(resource.getProject(), SVNProviderPlugin.getTypeId());
		if (teamProvider == null) return null;
		return teamProvider.getSVNWorkspaceRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNResource#getRepository()
	 */
	public ISVNRepositoryLocation getRepository()  {
		try {
		    SVNWorkspaceRoot root = getWorkspaceRoot();
		    if (root == null) {
		        SVNProviderPlugin.log(IStatus.WARNING, Policy.bind("LocalResource.errorGettingTeamProvider", resource.toString()), null);
		        return null;
		    }
			return root.getRepository();
		} catch (SVNException e) {
			// an exception is thrown when project is not managed
			SVNProviderPlugin.log(e);
			return null;
		}
	}

    /**
     * get the url of the resource in the repository
     * The resource does not need to exist in repository 
     * @return the url or null if cannot get the url (when project is not managed) 
     * @throws SVNException
     */
    public SVNUrl getUrl()
    {
    	try {
    		if (isManaged()) {
    			// if the resource is managed, get the url directly
    			return getStatus().getUrl();
    		} else {
    			// otherwise, get the url of the parent
    			ISVNLocalResource parent = getParent();
    			if (parent == null) {
    				return null; // we cannot find the url
    			}
   				return parent.getUrl().appendPath(resource.getName());	
    		}
    	} catch (SVNException e) {
    		return null;
    	}
    }

    /**
     * get the remote resource corresponding to the latest revision of this local resource 
     * @return null if there is no remote file corresponding to this local resource
     * @throws SVNException
     */
    public ISVNRemoteResource getLatestRemoteResource() throws SVNException {
        return getRemoteResource(SVNRevision.HEAD); 
    }

    /**
     * get the remote resource corresponding to the given revision of this local resource
     * @return null if there is no remote file corresponding to this local resource
     * @throws SVNException
     */
    public ISVNRemoteResource getRemoteResource(SVNRevision revision) throws SVNException {
        if (SVNRevision.BASE.equals(revision)) {
        	// if the user wants the base resource, we can't get it using the url
        	return getBaseResource();
        }
    	// even if file is not managed, there can be a corresponding resource
        GetRemoteResourceCommand command = new GetRemoteResourceCommand(getRepository(), getUrl(), revision);
        command.run(null);
        return command.getRemoteResource();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#delete()
     */
    public void delete() throws SVNException {
        try {
            ISVNClientAdapter svnClient = getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient);
            svnClient.remove(new File[] { getFile() }, true);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e); 
        } finally {
            OperationManager.getInstance().endOperation();
        }
    }

    /**
     * Restore pristine working copy file (undo all local edits) 
     */
    public void revert(boolean recurse) throws SVNException {
        try {
            ISVNClientAdapter svnClient = getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient);
            svnClient.revert(getFile(), recurse);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e); 
        } finally {
            OperationManager.getInstance().endOperation();
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#resolve()
     */
    public void resolve() throws SVNException {
        try {
            ISVNClientAdapter svnClient = getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient);
            svnClient.resolved(getFile());
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e); 
        } finally {
            OperationManager.getInstance().endOperation();
        }
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#setSvnProperty(java.lang.String, java.lang.String, boolean)
	 */
	public void setSvnProperty(String name, String value, boolean recurse) throws SVNException {
		try {
			ISVNClientAdapter svnClient = getRepository().getSVNClient();
			OperationManager.getInstance().beginOperation(svnClient);
			svnClient.propertySet(getFile(),name,value,recurse);
		} catch (SVNClientException e) {
			throw SVNException.wrapException(e); 
		} finally {
			OperationManager.getInstance().endOperation();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#setSvnProperty(java.lang.String, java.io.File, boolean)
	 */
	public void setSvnProperty(String name, File value, boolean recurse) throws SVNException {
		try {
			ISVNClientAdapter svnClient = getRepository().getSVNClient();
			OperationManager.getInstance().beginOperation(svnClient);
			svnClient.propertySet(getFile(),name,value,recurse);
		} catch (IOException e) {
			throw SVNException.wrapException(e);
		} catch (SVNClientException e) {
			throw SVNException.wrapException(e); 
		} finally {
			OperationManager.getInstance().endOperation();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#deleteSvnProperty(java.lang.String, boolean)
	 */
	public void deleteSvnProperty(String name,boolean recurse) throws SVNException {
		try {
			ISVNClientAdapter svnClient = getRepository().getSVNClient();
			OperationManager.getInstance().beginOperation(svnClient);
			svnClient.propertyDel(getFile(),name,recurse);
		} catch (SVNClientException e) {
			throw SVNException.wrapException(e); 
		} finally {
			OperationManager.getInstance().endOperation();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getSvnProperty(java.lang.String)
	 */
	public ISVNProperty getSvnProperty(String name) throws SVNException {
		try {
			ISVNClientAdapter svnClient = SVNProviderPlugin.getPlugin().createSVNClient();
			return svnClient.propertyGet(getFile(),name);
		} catch (SVNClientException e) {
	        throw SVNException.wrapException(e); 
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNLocalResource#getSvnProperties()
	 */
	public ISVNProperty[] getSvnProperties() throws SVNException {
		try {
			ISVNClientAdapter svnClient = getRepository().getSVNClient();
			ISVNProperty[] properties = svnClient.getProperties(getFile());
			return properties;
		} catch (SVNClientException e) {
			throw SVNException.wrapException(e); 
		}		
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNResource#getResource()
     */
    public IResource getResource() {
    	return resource;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(getIResource())) {
			return getIResource();
		}
		return Platform.getAdapterManager().getAdapter(this,adapter);
	}

}
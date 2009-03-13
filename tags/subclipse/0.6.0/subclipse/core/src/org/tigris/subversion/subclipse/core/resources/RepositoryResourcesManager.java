/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.repo.ISVNListener;
import org.tigris.subversion.svnclientadapter.SVNClientAdapter;

import com.qintsoft.jsvn.jni.ClientException;

/**
 * provides some static methods to handle repository management 
 * (deletion of remote resources etc ...)
 */
public class RepositoryResourcesManager {
    
    private List repositoryListeners = new ArrayList();


    /**
     * Register to receive notification of repository creation and disposal
     */
    public void addRepositoryListener(ISVNListener listener) {
        repositoryListeners.add(listener);
    }

    /**
     * De-register a listener
     */
    public void removeRepositoryListener(ISVNListener listener) {
        repositoryListeners.remove(listener);
    }

    /**
     * signals all listener that we have removed a repository 
     */
    public void repositoryRemoved(ISVNRepositoryLocation repository) {
        Iterator it = repositoryListeners.iterator();
        while (it.hasNext()) {
            ISVNListener listener = (ISVNListener)it.next();
            listener.repositoryRemoved(repository);
        }    
    }

    /**
     * signals all listener that we have removed a repository 
     */
    public void repositoryAdded(ISVNRepositoryLocation repository) {
        Iterator it = repositoryListeners.iterator();
        while (it.hasNext()) {
            ISVNListener listener = (ISVNListener)it.next();
            listener.repositoryAdded(repository);
        }    
    }
    
    /**
     * signals all listener that a remote resource has been created 
     */
    public void remoteResourceCreated(ISVNRemoteFolder parent, String resourceName) {
        Iterator it = repositoryListeners.iterator();
        while (it.hasNext()) {
            ISVNListener listener = (ISVNListener)it.next();
            listener.remoteResourceCreated(parent,resourceName);
        }    
    }    

    /**
     * signals all listener that a remote resource has been created 
     */
    public void remoteResourceDeleted(ISVNRemoteResource resource) {
        Iterator it = repositoryListeners.iterator();
        while (it.hasNext()) {
            ISVNListener listener = (ISVNListener)it.next();
            listener.remoteResourceDeleted(resource);
        }    
    } 
    
    /**
     * Creates a remote folder 
     */
    public void createRemoteFolder(ISVNRemoteFolder parent, String folderName, String message,IProgressMonitor monitor) throws SVNException {
        parent.createRemoteFolder(folderName, message, monitor);
    }

    /**
     * delete some remote resources
     * Resources can be from several RemoteRepositoryLocations 
     */
    public void deleteRemoteResources(ISVNRemoteResource[] remoteResources, String message,IProgressMonitor monitor) throws SVNException {
        IProgressMonitor progress = Policy.monitorFor(monitor);
        progress.beginTask(Policy.bind("RepositoryResourcesManager.deleteRemoteResources"), 100*remoteResources.length); //$NON-NLS-1$
        
        // the given remote resources can come from more than a repository and so needs
        // more than one svnClient
        // we associate each repository with the corresponding resources to delete
        HashMap mapRepositories = new HashMap();
        for (int i = 0;i < remoteResources.length;i++) {
            ISVNRemoteResource remoteResource = remoteResources[i];
            ISVNRepositoryLocation repositoryLocation = remoteResource.getRepository();
            List resources = (List)mapRepositories.get(repositoryLocation);
            if (resources == null) {
                resources = new ArrayList();
                mapRepositories.put(repositoryLocation, resources);
            }
            resources.add(remoteResource);
        }

        try {        
            for (Iterator it = mapRepositories.values().iterator(); it.hasNext();) {
                List resources = (List)it.next();
                SVNClientAdapter svnClient = ((ISVNRemoteResource)resources.get(0)).getRepository().getSVNClient();
                URL urls[] = new URL[resources.size()];
                for (int i = 0; i < resources.size();i++) {
                    ISVNRemoteResource resource = (ISVNRemoteResource)resources.get(i); 
                    urls[i] = resource.getUrl();
                    
                    // refresh just says that resource needs to be updated
                    // it does not update immediatly
                    resource.getParent().refresh();
                }
                svnClient.remove(urls,message);
                
                for (int i = 0; i < resources.size();i++) {
                    ISVNRemoteResource resource = (ISVNRemoteResource)resources.get(i);
                    remoteResourceDeleted(resource);
                }
                
                progress.worked(100*urls.length);
            }
        } catch (ClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            progress.done();
        }
    }


}
/*******************************************************************************
 * Copyright (c) 2003, 2004 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.core.internal.commands;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNInfo;
import org.eclipse.subversion.client.ISVNStatus;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNStatusKind;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.resources.LocalResourceStatus;

/**
 * Command to get the statuses of local resources
 */
public class GetStatusCommand implements ISVNCommand {
    private ISVNRepositoryLocation repository;
    private IResource resource;
    private boolean descend = true;
    private boolean getAll = true;
    private ISVNStatus[] svnStatuses;
    
    public GetStatusCommand(ISVNLocalResource svnResource, boolean descend, boolean getAll) {
    	this.repository = svnResource.getRepository();
    	this.resource = svnResource.getIResource();
        this.descend = descend;
        this.getAll = getAll;
    }

    public GetStatusCommand(ISVNRepositoryLocation repository, IResource resource, boolean descend, boolean getAll) {
    	this.repository = repository;
    	this.resource = resource;
        this.descend = descend;
        this.getAll = getAll;
    }    

    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws SVNException {
        ISVNClientAdapter svnClient = repository.getSVNClient();
        try { 
            svnStatuses = svnClient.getStatus(resource.getLocation().toFile(), descend, getAll);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        }
    }

    private LocalResourceStatus[] convert(ISVNStatus[] statuses) {
        LocalResourceStatus[] localStatuses = new LocalResourceStatus[statuses.length];
        for (int i = 0; i < statuses.length;i++) {
            localStatuses[i] = new LocalResourceStatus(statuses[i], getURL(statuses[i]));
        }
        return localStatuses;
    }

    // getStatuses returns null URL for svn:externals folder.  This will
    // get the URL using svn info command on the local resource
	private SVNUrl getURL(ISVNStatus status) {
		SVNUrl url = status.getUrl();
		if (url == null && !(status.getTextStatus() == SVNStatusKind.UNVERSIONED)) {
		    try { 
		    	ISVNClientAdapter svnClient = repository.getSVNClient();
		    	ISVNInfo info = svnClient.getInfoFromWorkingCopy(status.getFile());
		    	url = info.getUrl();
		    } catch (SVNException e) {
			} catch (SVNClientException e) {
			}
		}
		return url;
	}

    /**
     * get the results
     * @return
     */
    public ISVNStatus[] getStatuses() {
        return svnStatuses;
    } 

    /**
     * get the results
     * @return
     */
    public LocalResourceStatus[] getLocalResourceStatuses() {
        return convert(svnStatuses);
    }    
}

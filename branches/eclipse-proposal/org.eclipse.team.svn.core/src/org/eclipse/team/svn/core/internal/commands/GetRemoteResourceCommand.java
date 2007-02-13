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
package org.eclipse.team.svn.core.internal.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNInfo;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNNodeKind;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.resources.RemoteFile;
import org.eclipse.team.svn.core.internal.resources.RemoteFolder;

/**
 * Command to get a remote resource given a url and a revision
 */
public class GetRemoteResourceCommand implements ISVNCommand {
	private ISVNRepositoryLocation repository;
    private SVNUrl url;
    private SVNRevision revision;
    
    private ISVNRemoteResource remoteResource; 
    
    /**
     * revision must not be SVNRevision.BASE ! 
     * @param repository
     * @param url
     * @param revision
     */
    public GetRemoteResourceCommand(ISVNRepositoryLocation repository, SVNUrl url, SVNRevision revision) {
       this.repository = repository;
       this.url = url;
       this.revision = revision;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {
        monitor = Policy.monitorFor(monitor);
        monitor.beginTask(Policy.bind("GetRemoteResourceCommand.getLogEntries"), 100); //$NON-NLS-1$
        
        remoteResource = null;
        ISVNClientAdapter svnClient = repository.getSVNClient();
        ISVNInfo info;
        try {
            info = svnClient.getInfo(url, revision, revision);
        } catch (SVNClientException e) {
            throw new SVNException("Can't get remote resource "+url+" at revision "+revision,e);   
        }
        
        if (info == null) {
            remoteResource = null; // no remote file
        }
        else
        {
            if (info.getNodeKind() == SVNNodeKind.FILE)
                remoteResource = new RemoteFile(
                    null,  // we don't know its parent
                    repository,
                    url,
                    revision,
                    info.getLastChangedRevision(),
                    info.getLastChangedDate(),
                    info.getLastCommitAuthor()
                );
             else
                remoteResource = new RemoteFolder(
                    null,  // we don't know its parent
                    repository,
                    url,
                    revision,
                    info.getLastChangedRevision(),
                    info.getLastChangedDate(),
                    info.getLastCommitAuthor()
                );                
        }
        monitor.done();
	}

    /**
     * get the result of the command
     * @return
     */    
    public ISVNRemoteResource getRemoteResource() {
        return remoteResource;
    }
    
}

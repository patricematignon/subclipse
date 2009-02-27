/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.core.internal.resources;


import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.subversion.client.SVNRevision.Number;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;

/**
 * Whereas the RemoteFolder class provides access to a remote hierarchy using
 * lazy retrieval via <code>getMembers()</code>, the RemoteFolderTree will force 
 * a recursive retrieval of the remote hierarchy in one round trip.
 */
public class RemoteFolderTree extends RemoteFolder  {
    private static final ISVNRemoteResource[] EMPTY = new ISVNRemoteResource[] {};
    /**
     * @param resource
     * @param bytes
     */
    public RemoteFolderTree(IResource resource, byte[] bytes) {
        super(resource, bytes);
        this.children = EMPTY;
    }
    /**
     * @param repository
     * @param url
     * @param revision
     */
    public RemoteFolderTree(ISVNRepositoryLocation repository, SVNUrl url,
            SVNRevision revision) {
        super(repository, url, revision);
        this.children = EMPTY;
    }
    /**
     * @param parent
     * @param repository
     * @param url
     * @param revision
     * @param lastChangedRevision
     * @param date
     * @param author
     */
    public RemoteFolderTree(RemoteFolder parent,
            ISVNRepositoryLocation repository, SVNUrl url,
            SVNRevision revision, Number lastChangedRevision, Date date,
            String author) {
        super(parent, repository, url, revision, lastChangedRevision, date,
                author);
        this.children = EMPTY;
    }

	/* 
	 * This method is public to allow access by the RemoteFolderTreeBuilder utility class.
	 * No other external classes should use this method.
	 */
	public void setChildren(ISVNRemoteResource[] children) {
		this.children = children;
	}
}

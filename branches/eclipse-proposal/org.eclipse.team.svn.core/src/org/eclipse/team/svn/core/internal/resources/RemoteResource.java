/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.eclipse.team.svn.core.internal.resources;

import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.subversion.client.SVNUrlUtils;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.CachedResourceVariant;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.commands.GetLogsCommand;
import org.eclipse.team.svn.core.internal.history.AliasManager;
import org.eclipse.team.svn.core.internal.history.ILogEntry;

/**
 * The purpose of this class and its subclasses is to implement the corresponding
 * ISVNRemoteResource interfaces for the purpose of communicating information about 
 * resources that reside in a SVN repository but have not necessarily been loaded
 * locally.
 */
public abstract class RemoteResource
	extends CachedResourceVariant
	implements ISVNRemoteResource {

	protected RemoteFolder parent;
	// null when this is the repository location 
	protected SVNUrl url;
	protected ISVNRepositoryLocation repository;
    protected SVNRevision revision;
    protected SVNRevision.Number lastChangedRevision;
    protected Date date;
    protected String author;

	public RemoteResource(IResource local, byte[] bytes){
		String nfo = new String(bytes);
		
		lastChangedRevision = new SVNRevision.Number(Long.parseLong(nfo));
		revision = lastChangedRevision;
		ISVNLocalResource res = SVNWorkspaceRoot.getSVNResourceFor(local);

		url = res.getUrl();
		repository = res.getRepository();
	}
	
	/**
	 * Constructor for RemoteResource.
	 */
	public RemoteResource(
		RemoteFolder parent,
		ISVNRepositoryLocation repository,
		SVNUrl url,
        SVNRevision revision,
		SVNRevision.Number lastChangedRevision,
		Date date,
		String author) {

		this.parent = parent;
		this.repository = repository;
		this.url = url;
        this.revision = revision;
        
		this.lastChangedRevision = lastChangedRevision;
		this.date = date;
		this.author = author;
	}

    /**
     * this constructor is used for the folder corresponding to repository location
     */
    public RemoteResource(ISVNRepositoryLocation repository, SVNUrl url, SVNRevision revision) {
        this.parent = null;
        this.repository = repository;
        this.url = url;
        this.revision = revision;
        
        // we don't know the following properties
        this.lastChangedRevision = null;
        this.date = null;
        this.author = null;
    }

	/*
	 * @see ISVNRemoteResource#getName()
	 */
	public String getName() {
		return (url != null) ? url.getLastPathSegment() : "";
	}

    /**
     * get the path of this remote resource relatively to the repository
     */
    public String getRepositoryRelativePath() {
        return SVNUrlUtils.getRelativePath(getRepository().getUrl(), getUrl(), true);
    }    
	
    /*
	 * @see ISVNRemoteResource#exists(IProgressMonitor)
	 */
	public boolean exists(IProgressMonitor monitor) throws TeamException {
		
		return parent.exists(this, monitor);
	}
	
	/*
	 * @see ISVNRemoteResource#getParent()
	 */
	public ISVNRemoteFolder getParent() {
		return parent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object target) {
		if (this == target)
			return true;
		if (!(target instanceof RemoteResource))
			return false;
		RemoteResource remote = (RemoteResource) target;
		return remote.isContainer() == isContainer() && 
			remote.getUrl().equals(getUrl()) 
			&& remote.getRevision() == getRevision();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return getUrl().hashCode() + getRevision().hashCode();
	}

	public ISVNRepositoryLocation getRepository() {
		return repository;
	}

    /**
     * get the url of this remote resource
     */
    public SVNUrl getUrl() {
        return url;
    }

    /**
     * get the lastChangedRevision
     */
	public SVNRevision.Number getLastChangedRevision() {
		return lastChangedRevision;
	}

    /**
     * get the revision
     */
    public SVNRevision getRevision() {
        return revision;
    }

    /**
     * get the date 
     */
	public Date getDate() {
		return date;
	}

    /**
     * get the author
     */
	public String getAuthor() {
		return author;
	}

    /**
     * @see ISVNRemoteResource#getLogEntries()
     */
    public ILogEntry[] getLogEntries(IProgressMonitor monitor) throws SVNException {
        GetLogsCommand command = new GetLogsCommand(this);
        command.run(monitor);
        return command.getLogEntries();
    }

    /**
     * @see ISVNRemoteResource#getLogEntries()
     */
    public ILogEntry[] getLogEntries(IProgressMonitor monitor, SVNRevision pegRevision, SVNRevision revisionStart, SVNRevision revisionEnd, boolean stopOnCopy, long limit, AliasManager tagManager) throws TeamException {
        GetLogsCommand command = new GetLogsCommand(this);
        command.setPegRevision(pegRevision);
        command.setRevisionStart(revisionStart);
        command.setRevisionEnd(revisionEnd);
        command.setStopOnCopy(stopOnCopy);
        command.setLimit(limit);
        command.setTagManager(tagManager);
        command.run(monitor);
        return command.getLogEntries();    	
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.team.core.variants.IResourceVariant#getContentIdentifier()
     */
    public String getContentIdentifier() {
        if (getLastChangedRevision() == null) return "";
		return String.valueOf(getLastChangedRevision().getNumber());
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#getCachePath()
	 */
	protected String getCachePath() {
		return this.getUrl().toString() + ":" + getContentIdentifier();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#getCacheId()
	 */
	protected String getCacheId() {
		return SVNProviderPlugin.ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#asBytes()
	 */
	public byte[] asBytes() {
		return new Long(getContentIdentifier()).toString().getBytes();
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.ISVNResource#getResource()
     */
    public IResource getResource() {
    	return null;
    }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getCachePath();
	}
}

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
package org.tigris.subversion.subclipse.core.resources;

import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.CachedResourceVariant;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.history.LogEntry;
import org.tigris.subversion.subclipse.core.util.Util;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

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
	protected boolean hasProps;
    private SVNRevision revision;
	private SVNRevision.Number lastChangedRevision;
	private Date date;
	private String author;

	public RemoteResource(IResource local, byte[] bytes){
		String nfo = new String(bytes);
		
		lastChangedRevision = new SVNRevision.Number(Long.parseLong(nfo));
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
		boolean hasProps,
		SVNRevision.Number lastChangedRevision,
		Date date,
		String author) throws SVNException {

        if ( (revision.getKind() == SVNRevision.Kind.base) ||
             (revision.getKind() == SVNRevision.Kind.committed) ||
             (revision.getKind() == SVNRevision.Kind.previous) ) {
            throw new SVNException("A remote resource cannot have a base, committed or previous revision ");        
        }
		this.parent = parent;
		this.repository = repository;
		this.url = url;
        this.revision = revision;
        
		this.hasProps = hasProps;
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
        this.hasProps = false;
        this.lastChangedRevision = null;
        this.date = null;
        this.author = null;
    }


	/*
	 * @see ISVNRemoteResource#getName()
	 */
	public String getName() {
		return Util.getLastSegment(url.toString());
	}

    /**
     * get the path of this remote resource relatively to the repository
     */
    public String getRepositoryRelativePath() {
        return getUrl().toString().substring(getRepository().getUrl().toString().length());
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
     * tells if this remote resource has properties
     */
    public boolean getHasProps() {
        return hasProps;
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
    public LogEntry[] getLogEntries(IProgressMonitor monitor) throws SVNException {
        ISVNClientAdapter client = getRepository().getSVNClient();
        monitor = Policy.monitorFor(monitor);
        monitor.beginTask(Policy.bind("RemoteFile.getLogEntries"), 100); //$NON-NLS-1$
        
        ISVNLogMessage[] logMessages;
		try {
			logMessages =
				client.getLogMessages(
					getUrl(),
					new SVNRevision.Number(0),
					SVNRevision.HEAD);
		} catch (SVNClientException e) {
            throw SVNException.wrapException(e);
		}
        
        LogEntry[] logEntries = new LogEntry[logMessages.length];
        
        for (int i = 0; i < logMessages.length;i++) {
            logEntries[i] = new LogEntry(this, 
                              logMessages[i].getRevision(), 
                              logMessages[i].getAuthor(),
                              logMessages[i].getDate(),
                              logMessages[i].getMessage());
        }
             
        monitor.done();
        return logEntries;
    }

    public String getContentIdentifier() {
		return this.getLastChangedRevision().getNumber()+"";
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#getCachePath()
	 */
	protected String getCachePath() {
		return this.getUrl().toString()+":"+getContentIdentifier();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.CachedResourceVariant#getCacheId()
	 */
	protected String getCacheId() {
		return SVNProviderPlugin.ID;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.variants.IResourceVariant#asBytes()
	 */
	public byte[] asBytes() {
		System.out.println("returning bytes for: "+getCachePath());
		return new Long(getContentIdentifier()).toString().getBytes();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getCachePath();
	}
}

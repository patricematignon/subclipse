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
package org.eclipse.subversion.client.javahl;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;

import org.eclipse.subversion.client.ISVNStatus;
import org.eclipse.subversion.client.SVNNodeKind;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNStatusKind;
import org.eclipse.subversion.client.SVNUrl;
import org.tigris.subversion.javahl.Status;

/**
 * A JavaHL based implementation of {@link ISVNStatus}.
 * Actually just an adapter from {@link org.tigris.subversion.javahl.Status}
 *  
 * @author philip schatz
 */
public class JhlStatus implements ISVNStatus {

	private Status _s;

	/**
	 * Constructor
	 * @param status
	 */
	public JhlStatus(Status status) {
		// note that status.textStatus must be different than 0 (the resource must exist)
        super();
		_s = status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getUrl()
	 */
	public SVNUrl getUrl() {
		try {
            String url = _s.getUrl();
            return (url != null) ? new SVNUrl(url) : null;
        } catch (MalformedURLException e) {
            //should never happen.
            return null;
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getUrlString()
	 */
	public String getUrlString()
	{
		return _s.getUrl();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getLastChangedRevision()
	 */
	public SVNRevision.Number getLastChangedRevision() {
        // we don't use 
        // return (SVNRevision.Number)JhlConverter.convert(_s.getLastChangedRevision());
        // as _s.getLastChangedRevision() is currently broken if revision is -1 
		if (_s.getReposLastCmtAuthor() == null)
			return JhlConverter.convertRevisionNumber(_s.getLastChangedRevisionNumber());
		else
			if (_s.getReposLastCmtRevisionNumber() == 0)
				return null;
			return JhlConverter.convertRevisionNumber(_s.getReposLastCmtRevisionNumber());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getLastChangedDate()
	 */
	public Date getLastChangedDate() {
		if (_s.getReposLastCmtAuthor() == null)
			return _s.getLastChangedDate();
		else
			return _s.getReposLastCmtDate();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getLastCommitAuthor()
	 */
	public String getLastCommitAuthor() {
		if (_s.getReposLastCmtAuthor() == null)
			return _s.getLastCommitAuthor();
		else
			return _s.getReposLastCmtAuthor();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getTextStatus()
	 */
	public SVNStatusKind getTextStatus() {
        return JhlConverter.convertStatusKind(_s.getTextStatus());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getPropStatus()
	 */
	public SVNStatusKind getPropStatus() {
		return JhlConverter.convertStatusKind(_s.getPropStatus());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getRevision()
	 */
	public SVNRevision.Number getRevision() {
		return JhlConverter.convertRevisionNumber(_s.getRevisionNumber());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#isCopied()
	 */
	public boolean isCopied() {
		return _s.isCopied();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#isWcLocked()
	 */
	public boolean isWcLocked() {
		return _s.isLocked();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#isSwitched()
	 */
	public boolean isSwitched() {
		return _s.isSwitched();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getPath()
	 */
	public String getPath() {
		return _s.getPath();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getFile()
	 */
    public File getFile() {
        return new File(getPath()).getAbsoluteFile();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getNodeKind()
	 */
	public SVNNodeKind getNodeKind() {
		SVNNodeKind nodeKind;
		if (_s.getReposLastCmtAuthor() == null)
			nodeKind = JhlConverter.convertNodeKind(_s.getNodeKind());
		else
			nodeKind = JhlConverter.convertNodeKind(_s.getReposKind());
        return nodeKind;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getUrlCopiedFrom()
	 */
	public SVNUrl getUrlCopiedFrom() {
		try {
            String url = _s.getUrlCopiedFrom();
            return (url != null) ? new SVNUrl(url) : null;
        } catch (MalformedURLException e) {
            //should never happen.
            return null;
        }
	}

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getRepositoryTextStatus()
     */
    public SVNStatusKind getRepositoryTextStatus() {
        return JhlConverter.convertStatusKind(_s.getRepositoryTextStatus());
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getRepositoryPropStatus()
     */
    public SVNStatusKind getRepositoryPropStatus() {
        return JhlConverter.convertStatusKind(_s.getRepositoryPropStatus());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getPath() + " "+getTextStatus().toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getConflictNew()
     */
    public File getConflictNew() {
		String path = _s.getConflictNew();
		return (path != null) ? new File(getFile().getParent(), path)
				.getAbsoluteFile() : null;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.subversion.client.ISVNStatus#getConflictOld()
	 */
    public File getConflictOld() {
		String path = _s.getConflictOld();
		return (path != null) ? new File(getFile().getParent(), path)
				.getAbsoluteFile() : null;
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.subversion.client.ISVNStatus#getConflictWorking()
	 */
    public File getConflictWorking() {
		String path = _s.getConflictWorking();
		return (path != null) ? new File(getFile().getParent(), path)
				.getAbsoluteFile() : null;
	}
    
    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.subversion.client.ISVNStatus#getLockCreationDate()
	 */
    public Date getLockCreationDate() {
        return _s.getLockCreationDate();
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getLockOwner()
     */
    public String getLockOwner() {
        return _s.getLockOwner();
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getLockComment()
     */
    public String getLockComment() {
        return _s.getLockComment();
    }
}
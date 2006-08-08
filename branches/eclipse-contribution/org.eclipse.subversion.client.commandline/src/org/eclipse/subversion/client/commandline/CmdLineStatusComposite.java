/*******************************************************************************
 * Copyright (c) 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.subversion.client.commandline;

import java.io.File;
import java.util.Date;

import org.eclipse.subversion.client.ISVNStatus;
import org.eclipse.subversion.client.SVNNodeKind;
import org.eclipse.subversion.client.SVNStatusKind;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.subversion.client.SVNRevision.Number;

/**
 * <p>
 * Implements a ISVNStatus using "svn status" and "svn info".</p>
 * 
 * @author Philip Schatz (schatz at tigris)
 * @author Cédric Chabanois (cchabanois at no-log.org)
 * @author Daniel Rall
 */
class CmdLineStatusComposite  implements ISVNStatus {
    private CmdLineStatusPart statusPart;
    private CmdLineInfoPart infoPart;

	/**
	 * <p>
	 * Creates a new status 
	 * </p>
     * Don't use this constructor if statusPart is null : use CmdLineStatusUnversioned instead 
	 * @param statusLinePart Generated from "svn status"
	 * @param infoLinePart Generated from "svn info"
	 */
	CmdLineStatusComposite(CmdLineStatusPart statusPart, CmdLineInfoPart infoPart) {
        this.statusPart = statusPart;
        this.infoPart = infoPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNClientStatus#getTextStatus()
	 */
	public SVNStatusKind getTextStatus() {
        return statusPart.getTextStatus();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getPropStatus()
	 */
	public SVNStatusKind getPropStatus() {
		return statusPart.getPropStatus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNClientStatus#getUrlCopiedFrom()
	 */
	public SVNUrl getUrlCopiedFrom() {
		return infoPart.getCopyUrl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getLastChangedDate()
	 */
	public Date getLastChangedDate() {
		return infoPart.getLastChangedDate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getLastChangedRevision()
	 */
	public Number getLastChangedRevision() {
		return infoPart.getLastChangedRevision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getLastCommitAuthor()
	 */
	public String getLastCommitAuthor() {
		return infoPart.getLastCommitAuthor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getNodeKind()
	 */
	public SVNNodeKind getNodeKind() {
		return infoPart.getNodeKind();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getPath()
	 */
	public String getPath() {
		return (infoPart != null) ? infoPart.getPath() : statusPart.getPath();
	}
    
    /**
     * @return The absolute path to this item.
     */
    public File getFile() {
        return statusPart.getFile();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getRevision()
	 */
	public Number getRevision() {
		return infoPart.getRevision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getUrl()
	 */
	public SVNUrl getUrl() {
		return infoPart.getUrl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getUrlString()
	 */
	public String getUrlString()
	{
		return infoPart.getUrlString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getRepositoryTextStatus()
	 */
    public SVNStatusKind getRepositoryTextStatus() {
        return statusPart.getRepositoryTextStatus();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getRepositoryPropStatus()
     */
    public SVNStatusKind getRepositoryPropStatus() {
        return statusPart.getRepositoryPropStatus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getConflictNew()
     */
    public File getConflictNew() {
        return infoPart.getConflictNew();
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getConflictOld()
     */
    public File getConflictOld() {
        return infoPart.getConflictOld();
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getConflictWorking()
     */
    public File getConflictWorking() {
        return infoPart.getConflictWorking();
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#isCopied()
     */
    public boolean isCopied() {
        return statusPart.isCopied();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#isWcLocked()
	 */
	public boolean isWcLocked() {
		return statusPart.isWcLocked();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#isSwitched()
	 */
	public boolean isSwitched() {
		return statusPart.isSwitched();
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.subversion.client.ISVNStatus#getLockCreationDate()
	 */
    public Date getLockCreationDate() {
        return infoPart.getLockCreationDate();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getLockOwner()
     */
    public String getLockOwner() {
        return infoPart.getLockOwner();
    }
 
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getLockComment()
     */
    public String getLockComment() {
        return infoPart.getLockComment();
    }
    
    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return statusPart.getPath() + "  T: " + statusPart.getTextStatus()
				+ " P: " + statusPart.getPropStatus();
	}
}

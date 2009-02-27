/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.subversion.client;

import java.io.File;
import java.util.Date;

/**
 * A special {@link ISVNStatus} implementation that is used if a File/Folder is not versioned or is ignored.
 * 
 * @author Philip Schatz (schatz at tigris)
 * @author C�dric Chabanois (cchabanois at no-log.org)
 */
public class SVNStatusUnversioned implements ISVNStatus {
    private File file;
    private boolean isIgnored = false;
	
    /**
     * Constructor.
     * @param file
     * @param isIgnored true when creating {@link SVNStatusKind#IGNORED}, otherwise {@link SVNStatusKind#UNVERSIONED}
     */
    public SVNStatusUnversioned(File file, boolean isIgnored) {
        this.file = file;
        // A file can be both unversioned and ignored.
        this.isIgnored = isIgnored;
    }
    
	/**
	 * Constructor.
	 * Creates an unversioned status (i.e. not ignored)
	 * @param file
	 */
	public SVNStatusUnversioned(File file) {
		this.file = file;
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getPath()
	 */
	public String getPath() {
		return file.getPath();
	}

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getFile()
     */
    public File getFile() {
        return file.getAbsoluteFile();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getUrl()
	 */
	public SVNUrl getUrl() {
		return null;
	}

    /*
	 * @see org.eclipse.subversion.client.ISVNStatus#getUrlString()
	 */
	public String getUrlString()
	{
		return null;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getLastChangedRevision()
	 */
	public SVNRevision.Number getLastChangedRevision() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getLastChangedDate()
	 */
	public Date getLastChangedDate() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getLastCommitAuthor()
	 */
	public String getLastCommitAuthor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getTextStatus()
	 */
	public SVNStatusKind getTextStatus() {
        if (isIgnored) {
        	return SVNStatusKind.IGNORED;
        }
    	return SVNStatusKind.UNVERSIONED;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getPropStatus()
	 */
	public SVNStatusKind getPropStatus() {
	     //As this status does not describe a managed resource, we
	     //cannot pretend that there is property status, and thus always
	     //{@link SVNStatusKind#NONE}.
		return SVNStatusKind.NONE;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getRepositoryTextStatus()
     */
    public SVNStatusKind getRepositoryTextStatus() {
        return SVNStatusKind.UNVERSIONED;
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getRepositoryPropStatus()
     */
    public SVNStatusKind getRepositoryPropStatus() {
        return SVNStatusKind.UNVERSIONED;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getRevision()
	 */
	public SVNRevision.Number getRevision() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#isCopied()
	 */
	public boolean isCopied() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#isWcLocked()
	 */
	public boolean isWcLocked() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#isSwitched()
	 */
	public boolean isSwitched() {
		return false;
	}

	/* (non-Javadoc)
	 * @see
	 * @see org.eclipse.subversion.client.ISVNStatus#getNodeKind()
	 */
	public SVNNodeKind getNodeKind() {
		//As this status does not describe a managed resource, we
	    //cannot pretend to know the node kind, and thus always return
	    //{@link SVNNodeKind#UNKNOWN}.
        return SVNNodeKind.UNKNOWN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNStatus#getUrlCopiedFrom()
	 */
	public SVNUrl getUrlCopiedFrom() {
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getConflictNew()
     */
    public File getConflictNew() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getConflictOld()
     */
    public File getConflictOld() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getConflictWorking()
     */
    public File getConflictWorking() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getLockComment()
     */
    public String getLockComment() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getLockCreationDate()
     */
    public Date getLockCreationDate() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNStatus#getLockOwner()
     */
    public String getLockOwner() {
        return null;
    }
}
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

import org.eclipse.subversion.client.SVNRevision.Number;

/**
 * A special {@link ISVNInfo} implementation that is used if a File/Folder is not versioned.
 * 
 * @author Cédric Chabanois (cchabanois at no-log.org)
 */
public class SVNInfoUnversioned implements ISVNInfo {
	private File file;
    
	/**
	 * Constructor
	 * @param file
	 */
    public SVNInfoUnversioned(File file) {
    	this.file = file;
    }
    
    
	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getFile()
	 */
	public File getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getUrl()
	 */
	public SVNUrl getUrl() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getUuid()
	 */
	public String getUuid() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getRepository()
	 */
	public SVNUrl getRepository() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getSchedule()
	 */
	public SVNScheduleKind getSchedule() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getNodeKind()
	 */
	public SVNNodeKind getNodeKind() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastCommitAuthor()
	 */
	public String getLastCommitAuthor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getRevision()
	 */
	public Number getRevision() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastChangedRevision()
	 */
	public Number getLastChangedRevision() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastChangedDate()
	 */
	public Date getLastChangedDate() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastDateTextUpdate()
	 */
	public Date getLastDateTextUpdate() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastDatePropsUpdate()
	 */
	public Date getLastDatePropsUpdate() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#isCopied()
	 */
	public boolean isCopied() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getCopyRev()
	 */
	public Number getCopyRev() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getCopyUrl()
	 */
	public SVNUrl getCopyUrl() {
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNInfo#getLockCreationDate()
     */
    public Date getLockCreationDate() {
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNInfo#getLockOwner()
     */
    public String getLockOwner() {
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNInfo#getLockComment()
     */
    public String getLockComment() {
        return null;
    }
}

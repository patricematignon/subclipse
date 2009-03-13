/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.subversion.client.javasvn;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.eclipse.subversion.client.ISVNInfo;
import org.eclipse.subversion.client.SVNNodeKind;
import org.eclipse.subversion.client.SVNScheduleKind;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.subversion.client.SVNRevision.Number;
import org.tigris.subversion.javahl.Info;

/**
 * A JavaHL based implementation of {@link ISVNInfo}.
 * Actually just an adapter from {@link org.tigris.subversion.javahl.Info}
 *  
 * @author C�dric Chabanois
 */
public class JhlInfo implements ISVNInfo {
	
	private Info info;
	private File file;

	/**
	 * Constructor
	 * @param file
	 * @param info
	 */
	public JhlInfo(File file, Info info) {
        super();
        this.file = file;
        this.info = info;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getFile()
	 */
	public File getFile() {
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getUrl()
	 */
	public SVNUrl getUrl() {
		try {
			return new SVNUrl(info.getUrl());
		} catch (MalformedURLException e) {
            //should never happen.
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getUuid()
	 */
	public String getUuid() {
		return info.getUuid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getRepository()
	 */
	public SVNUrl getRepository() {
		try {
			return new SVNUrl(info.getRepository());
		} catch (MalformedURLException e) {
            //should never happen.
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getSchedule()
	 */
	public SVNScheduleKind getSchedule() {
		return JhlConverter.convertScheduleKind(info.getSchedule());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getNodeKind()
	 */
	public SVNNodeKind getNodeKind() {
		return JhlConverter.convertNodeKind(info.getNodeKind());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getAuthor()
	 */
	public String getLastCommitAuthor() {
		return info.getAuthor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getRevision()
	 */
	public Number getRevision() {
		return JhlConverter.convertRevisionNumber(info.getRevision());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastChangedRevision()
	 */
	public Number getLastChangedRevision() {
		return JhlConverter.convertRevisionNumber(info.getLastChangedRevision());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastChangedDate()
	 */
	public Date getLastChangedDate() {
		return info.getLastChangedDate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastDateTextUpdate()
	 */
	public Date getLastDateTextUpdate() {
		return info.getLastDateTextUpdate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getLastDatePropsUpdate()
	 */
	public Date getLastDatePropsUpdate() {
		return info.getLastDatePropsUpdate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#isCopied()
	 */
	public boolean isCopied() {
		return (info.getCopyRev() > 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getCopyRev()
	 */
	public Number getCopyRev() {
		return JhlConverter.convertRevisionNumber(info.getCopyRev());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNInfo#getCopyUrl()
	 */
	public SVNUrl getCopyUrl() {
		try {
			return new SVNUrl(info.getCopyUrl());
		} catch (MalformedURLException e) {
            //should never happen.
			return null;
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNInfo#getLockCreationDate()
     */
    public Date getLockCreationDate() {
    	//Not available in info(1)
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNInfo#getLockOwner()
     */
    public String getLockOwner() {
    	//Not available in info(1)
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNInfo#getLockComment()
     */
    public String getLockComment() {
    	//Not available in info(1)
        return null;
    }
}
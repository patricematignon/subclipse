/*
 *  Copyright(c) 2003-2004 by the authors indicated in the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tigris.subversion.svnclientadapter.javahl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import org.tigris.subversion.javahl.Info;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;

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
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getFile()
	 */
	public File getFile() {
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getUrl()
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
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getUuid()
	 */
	public String getUuid() {
		return info.getUuid();
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getRepository()
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
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getSchedule()
	 */
	public SVNScheduleKind getSchedule() {
		return JhlConverter.convertScheduleKind(info.getSchedule());
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getNodeKind()
	 */
	public SVNNodeKind getNodeKind() {
		return JhlConverter.convertNodeKind(info.getNodeKind());
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getAuthor()
	 */
	public String getLastCommitAuthor() {
		return info.getAuthor();
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getRevision()
	 */
	public Number getRevision() {
		return JhlConverter.convertRevisionNumber(info.getRevision());
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastChangedRevision()
	 */
	public Number getLastChangedRevision() {
		return JhlConverter.convertRevisionNumber(info.getLastChangedRevision());
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastChangedDate()
	 */
	public Date getLastChangedDate() {
		return info.getLastChangedDate();
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastDateTextUpdate()
	 */
	public Date getLastDateTextUpdate() {
		return info.getLastDateTextUpdate();
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastDatePropsUpdate()
	 */
	public Date getLastDatePropsUpdate() {
		return info.getLastDatePropsUpdate();
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#isCopied()
	 */
	public boolean isCopied() {
		return (info.getCopyRev() > 0);
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getCopyRev()
	 */
	public Number getCopyRev() {
		return JhlConverter.convertRevisionNumber(info.getCopyRev());
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getCopyUrl()
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
     * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLockCreationDate()
     */
    public Date getLockCreationDate() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLockOwner()
     */
    public String getLockOwner() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLockComment()
     */
    public String getLockComment() {
        return null;
    }
}

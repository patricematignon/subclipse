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
package org.tigris.subversion.svnclientadapter.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;

/**
 * Represents the infos for one resource in the result of a svn info command
 * 
 * @author Philip Schatz (schatz at tigris)
 * @author C�dric Chabanois (cchabanois at no-log.org)
 */
class CmdLineInfoPart implements ISVNInfo {

	//"Constants"
	private static final String KEY_PATH = "Path";
	private static final String KEY_URL = "URL";
	private static final String KEY_REVISION = "Revision";
	private static final String KEY_REPOSITORY = "Repository";
	private static final String KEY_NODEKIND = "Node Kind";
	private static final String KEY_LASTCHANGEDAUTHOR = "Last Changed Author";
	private static final String KEY_LASTCHANGEDREV = "Last Changed Rev";
	private static final String KEY_LASTCHANGEDDATE = "Last Changed Date";
	private static final String KEY_TEXTLASTUPDATED = "Text Last Updated";
	private static final String KEY_SCHEDULE = "Schedule";
	private static final String KEY_COPIEDFROMURL = "Copied From URL";
	private static final String KEY_COPIEDFROMREV = "Copied From Rev";
	private static final String KEY_PROPSLASTUPDATED = "Properties Last Updated";
	private static final String KEY_REPOSITORYUUID = "Repository UUID";
	private static final String KEY_LOCKOWNER = "Lock Owner";
	private static final String KEY_LOCKCREATIONDATE = "Lock Created";
	private static final String KEY_LOCKCOMMENT = "Lock Comment";

	//Fields
	private Map infoMap = new HashMap();
	private boolean unversioned = false;

	//Constructors
    /** 
     * Here is two samples for infostring parameter
     * sample 1 :
     * ==========
     * Path: added.txt
     * Name: added.txt
     * URL: file:///F:/Programmation/Projets/subversion/svnant/test/test_repos/statusT
     * st/added.txt
     * Revision: 0
     * Node Kind: file
     * Schedule: add
     *  
     * sample 2 :
     * ===========
     * ignored.txt:  (Not a versioned resource)
     */
	CmdLineInfoPart(String infoString) {
		load(infoString);
	}


	/*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastChangedDate()
	 */
	public Date getLastChangedDate() {
		return (unversioned) ? null : Helper.toDate(get(KEY_LASTCHANGEDDATE));
	}

	/*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastChangedRevision()
	 */
	public SVNRevision.Number getLastChangedRevision() {
		return (unversioned) ? null : Helper.toRevNum(get(KEY_LASTCHANGEDREV));
	}

	/*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastCommitAuthor()
	 */
	public String getLastCommitAuthor() {
		return (unversioned) ? null : get(KEY_LASTCHANGEDAUTHOR);
	}

	public SVNNodeKind getNodeKind() {
		return (unversioned) ? null : SVNNodeKind.fromString(get(KEY_NODEKIND));
	}

	public String getPath() {
		return get(KEY_PATH);
	}

	/*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getFile()
	 */
    public File getFile() {
        return new File(getPath()).getAbsoluteFile();
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getRevision()
     */
	public SVNRevision.Number getRevision() {
		return (unversioned) ? SVNRevision.INVALID_REVISION : Helper.toRevNum(get(KEY_REVISION));
	}

	/*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getUrl()
	 */
	public SVNUrl getUrl() {
		return (unversioned) ? null : Helper.toSVNUrl(get(KEY_URL));
	}

	private String get(String key) {
		Object value = infoMap.get(key);
		return (value == null) ? null : value.toString();
	}

	private void load(String infoString) {
		StringTokenizer st = new StringTokenizer(infoString, Helper.NEWLINE);

		//this does not have to be a versioned resource.
		//if it is not, the first line will end with
		// ":  (Not a versioned resource)"
		if (st.countTokens() == 1) {
			unversioned = true;
            String line = st.nextToken();
            infoMap.put(KEY_PATH,line.substring(0,line.indexOf(":  (Not a versioned resource)")));
		} else {

			//First, go through and take each line and throw
			// it into a map with the key being the text to
			// the left of the colon, and value being to the
			// right.
		    StringBuffer lockComment = new StringBuffer();
		    boolean inComment = false;
			while (st.hasMoreTokens()) {
				String line = st.nextToken();
				if (inComment) {
				    lockComment.append(line).append("\n");
				} else {
					int middle = line.indexOf(':');
					String key = line.substring(0, middle);
					if (key.startsWith(KEY_LOCKCOMMENT)) 
					    inComment = true;
					else {
						String value = line.substring(middle + 2);
						infoMap.put(key, value);
					}
				}
			}
			if (inComment)
			    infoMap.put(KEY_LOCKCOMMENT, lockComment.toString());
		}
	}
    
    public boolean isVersioned() {
        return !unversioned;
    }

    
	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastDateTextUpdate()
	 */
	public Date getLastDateTextUpdate() {
		return (unversioned) ? null : Helper.toDate(get(KEY_TEXTLASTUPDATED));	
	}


	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getUuid()
	 */
	public String getUuid() {
		return (unversioned) ? null : get(KEY_REPOSITORYUUID);
	}


	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getRepository()
	 */
	public SVNUrl getRepository() {
		return (unversioned) ? null : Helper.toSVNUrl(get(KEY_REPOSITORY));
	}


	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getSchedule()
	 */
	public SVNScheduleKind getSchedule() {
		return SVNScheduleKind.fromString(get(KEY_SCHEDULE));
	}


	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLastDatePropsUpdate()
	 */
	public Date getLastDatePropsUpdate() {
		return (unversioned) ? null : Helper.toDate(get(KEY_PROPSLASTUPDATED));
	}


	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#isCopied()
	 */
	public boolean isCopied() {
		return (getCopyRev() != null) || (getCopyUrl() != null);
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getCopyRev()
	 */
	public Number getCopyRev() {
		return (unversioned) ? null : Helper.toRevNum(get(KEY_COPIEDFROMREV));
	}


	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getCopyUrl()
	 */
	public SVNUrl getCopyUrl() {
		return (unversioned) ? null : Helper.toSVNUrl(get(KEY_COPIEDFROMURL));
	}

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLockCreationDate()
     */
    public Date getLockCreationDate() {
 		return (unversioned) ? null : Helper.toDate(get(KEY_LOCKCREATIONDATE));
    }
    
    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLockOwner()
     */
    public String getLockOwner() {
		return (unversioned) ? null : get(KEY_LOCKOWNER);
    }
    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNInfo#getLockComment()
     */
    public String getLockComment() {
		return (unversioned) ? null : get(KEY_LOCKCOMMENT);
    }
    
    public static String[] parseInfoParts(String infoLines) {
		StringTokenizer st = new StringTokenizer(infoLines, Helper.NEWLINE+Helper.NEWLINE);
		String current = null;
		List infoParts = new ArrayList(st.countTokens());
		while (st.hasMoreTokens()){
		    String temp = st.nextToken();
		    if (temp.startsWith("Path:") || temp.endsWith(":  (Not a versioned resource)")){
		        if (current != null)
		            infoParts.add(current);
		        current = temp;
		    } else {
	            if (current == null)
	                current = temp;
	            else
	                current += "\n" + temp;
		    }
		}
		if (current!= null)
            infoParts.add(current);
        String[] infoArray = new String[infoParts.size()];
        infoParts.toArray(infoArray);
        return infoArray;
    }
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion  
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.history;

 
import java.util.Date;

import org.eclipse.core.runtime.PlatformObject;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * represent an entry for a SVN file that results
 * from the svn log command.
 */
public class LogEntry extends PlatformObject implements ILogEntry {

	private ISVNRemoteResource resource; // the corresponding remote resource
    private ISVNLogMessage logMessage;

    
	public LogEntry(ISVNLogMessage logMessage, ISVNRemoteResource resource) {
        this.logMessage = logMessage;
        this.resource = resource;
	}
	
	/**
	 * @see ILogEntry#getRevision()
	 */
	public SVNRevision.Number getRevision() {
		return logMessage.getRevision();
	}

	/**
	 * @see ILogEntry#getAuthor()
	 */
	public String getAuthor() {
		return logMessage.getAuthor();
	}

	/**
	 * @see ILogEntry#getDate()
	 */
	public Date getDate() {
		return logMessage.getDate();
	}

	/**
	 * @see ILogEntry#getComment()
	 */
	public String getComment() {
		return logMessage.getMessage();
	}

	/**
	 * @see ILogEntry#getRemoteFile()
	 */
	public ISVNRemoteResource getRemoteResource() {
		return resource;
	}
    
    public LogEntryChangePath[] getLogEntryChangePaths() {
    	ISVNLogMessageChangePath[] changePaths = logMessage.getChangedPaths();
        LogEntryChangePath[] logEntryChangePaths = new LogEntryChangePath[changePaths.length]; 
        for (int i = 0; i < changePaths.length; i++) {
        	logEntryChangePaths[i] = new LogEntryChangePath(this,changePaths[i]);
        }
        return logEntryChangePaths;
    }
    

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(resource)) {
			return resource;
		}
		return super.getAdapter(adapter);
	}
}


/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion  
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.history;

import java.net.MalformedURLException;

import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * A changePath in LogEntry
 * @see ISVNLogMessageChangePath
 */
public class LogEntryChangePath {

    private SVNUrl path;
    private ISVNLogMessageChangePath logMessageChangePath;
    private ILogEntry logEntry; 
    
    public LogEntryChangePath(ILogEntry logEntry, ISVNLogMessageChangePath logMessageChangePath) {
        this.logMessageChangePath = logMessageChangePath;
        this.logEntry = logEntry;
    }
    
    /**
     * Retrieve the path to the commited item
     * @return  the path to the commited item
     */
    public String getPath() {
    	return logMessageChangePath.getPath();
    }

    /**
     * Retrieve the copy source revision (if any)
     * @return  the copy source revision (if any)
     */
    public SVNRevision.Number getCopySrcRevision() {
    	return logMessageChangePath.getCopySrcRevision();
    }

    /**
     * Retrieve the copy source path (if any)
     * @return  the copy source path (if any)
     */
    public String getCopySrcPath() {
    	return logMessageChangePath.getCopySrcPath();
    }

    /**
     * Retrieve action performed
     * @return  action performed
     */
    public char getAction() {
        return logMessageChangePath.getAction();
    }
    
    public SVNUrl getUrl() {
    	SVNUrl repositoryRoot = logEntry.getResource().getRepository().getRepositoryRoot();
        if (repositoryRoot != null) {
            try {
				return new SVNUrl(repositoryRoot.get()+getPath());
			} catch (MalformedURLException e) {
				return null;
			}
        } else {
        	return null;
        }
    }
    
}

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
package org.tigris.subversion.subclipse.core.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNRevision;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.client.OperationManager;

/**
 * Changes the commit comment on a previously committed revision
 * 
 * @author Jesper Steen M�ller (jespersm at tigris.org) 
 */
public class ChangeCommitPropertiesCommand implements ISVNCommand {
	private ISVNRepositoryLocation repositoryLocation;
    private SVNRevision.Number revisionNo;
    private String logMessage;
    private String author;
    
    public ChangeCommitPropertiesCommand(ISVNRepositoryLocation theRepositoryLocation, SVNRevision.Number theRevisionNo, String theLogMessage, String theAuthor) {
    	this.repositoryLocation = theRepositoryLocation; 
        this.revisionNo = theRevisionNo;
        this.logMessage = theLogMessage;
        this.author = theAuthor;
    }
        
	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.core.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {
        monitor.beginTask(null, 100); //$NON-NLS-1$

        ISVNClientAdapter svnClient = repositoryLocation.getSVNClient();
        try {
            OperationManager.getInstance().beginOperation(svnClient);
            
            try {
            	if (logMessage != null)
            		svnClient.setRevProperty(repositoryLocation.getUrl(), revisionNo, "svn:log", logMessage, true);
            	if (author != null)
            		svnClient.setRevProperty(repositoryLocation.getUrl(), revisionNo, "svn:author", author, true);
            }
            catch (SVNClientException e) {
                throw SVNException.wrapException(e);
            }

        } finally {
            OperationManager.getInstance().endOperation();
            monitor.done();
        }
	}
}

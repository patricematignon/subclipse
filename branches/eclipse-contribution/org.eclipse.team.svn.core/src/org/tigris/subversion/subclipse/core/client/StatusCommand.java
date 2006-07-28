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
package org.tigris.subversion.subclipse.core.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNNotifyListener;
import org.eclipse.subversion.client.ISVNStatus;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNNodeKind;
import org.eclipse.subversion.client.SVNRevision;

/**
 * @author Panagiotis K
 */
public class StatusCommand {
    private final File file;
    private final boolean descend;
    private final boolean getAll;
    private final boolean contactServer;

    private ISVNStatus[] statuses;
    
    /** List storing RevisionsCache objects as reported by logRevision() 
     *  They have to be sorted in descending order, so more specific (deeper in the tree)
     *  is looked up first */
    protected List revisions = new ArrayList();

    public StatusCommand(File file, boolean descend, boolean getAll, boolean contactServer) {
        this.file = file;
        this.descend = descend;
        this.getAll = getAll;
        this.contactServer = contactServer;
    }

    protected void execute(final ISVNClientAdapter client, final IProgressMonitor monitor) throws SVNClientException {
        ISVNNotifyListener revisionListener = new ISVNNotifyListener() {
            public void setCommand(int command) {}
            public void logCommandLine(String commandLine) {}
            public void logMessage(String message) {}
            public void logError(String message) {}
            public void logRevision(long aRevision, String path) {
                StatusCommand.this.revisions.add(new RevisionsCache(aRevision, path));
                if (StatusCommand.this.revisions.size() > 1)
                {
                	Collections.sort(StatusCommand.this.revisions);
                }
            }
            public void logCompleted(String message) {}
            public void onNotify(File path, SVNNodeKind kind) {}
        };

        try{
            client.addNotifyListener( revisionListener );
            statuses = client.getStatus( file, descend, getAll, contactServer );
        }
        finally {
            client.removeNotifyListener( revisionListener );
        }
    }

    public ISVNStatus[] getStatuses() {
        return statuses;
    }

    protected SVNRevision.Number getRevisionFor(ISVNStatus status) {
    	if (revisions.size() == 1)
    	{
    		return ((RevisionsCache) revisions.get(0)).getRevision();
    	}
    	else
    	{
    		for (Iterator it = revisions.iterator(); it.hasNext();) {
				RevisionsCache element = (RevisionsCache) it.next();
				if (element.appliesFor(status.getPath()))
				{
					return element.getRevision();
				}				
			}
    		return SVNRevision.INVALID_REVISION;
    	}
    }
    
    private static class RevisionsCache implements Comparable {
    	private final long revision;
    	private final String path;
    	
    	protected RevisionsCache(long revision, String path)    	
    	{
    		this.revision = revision;
    		this.path = path;
    	}

		protected String getPath() {
			return path;
		}

		protected SVNRevision.Number getRevision() {
			return new SVNRevision.Number(revision);
		}
		
		protected boolean appliesFor(String statusPath)
		{
			return statusPath.startsWith(this.path);
		}
    
		public int compareTo(Object o2) {
			return ((RevisionsCache) o2).getPath().compareTo(this.getPath());
		}
    }
    
}

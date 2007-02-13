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
package org.eclipse.team.svn.core.internal.client;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNInfo;
import org.eclipse.subversion.client.ISVNNotifyListener;
import org.eclipse.subversion.client.ISVNStatus;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNNodeKind;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNStatusKind;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.resources.LocalResourceStatus;

/**
 * Peek for (get) the resource status.
 * Do not descend to children and DO NOT affect sync cache in any way !
 * This command should have no side effects.
 */
public class PeekStatusCommand {
    private final IResource resource;

    private ISVNStatus status = null;
    private ISVNInfo info = null;
    protected SVNRevision.Number revision;

    public PeekStatusCommand(IResource resource) {
        this.resource = resource;
    }

    public void execute(ISVNClientAdapter client) throws SVNException {
        ISVNNotifyListener revisionListener = new ISVNNotifyListener() {
            public void setCommand(int command) {}
            public void logCommandLine(String commandLine) {}
            public void logMessage(String message) {}
            public void logError(String message) {}
            public void logRevision(long aRevision, String path) {
                PeekStatusCommand.this.revision = new SVNRevision.Number(aRevision);
            }
            public void logCompleted(String message) {}
            public void onNotify(File path, SVNNodeKind kind) {}
        };

        try{
            client.addNotifyListener( revisionListener );
            File file = resource.getLocation().toFile();
            status = null;
            ISVNStatus[] statuses = client.getStatus( file, false, true, false);
            for (int i = 0; i < statuses.length; i++) {
				if (file.equals(statuses[i].getFile()))
				{
					status = statuses[i];
					if (status.getUrl() == null && !(status.getTextStatus() == SVNStatusKind.UNVERSIONED))
						info = client.getInfo(status.getFile());
					break;
				}
			}
        }
        catch (SVNClientException e) {
        	throw SVNException.wrapException(e);
        }
        finally {
            client.removeNotifyListener( revisionListener );
        }
    }

    public ISVNStatus getStatus() {
        return status;
    }

    public LocalResourceStatus getLocalResourceStatus()
    {    	
    	return (status != null) ? new LocalResourceStatus(status, getURL(status)) : null;
    }
    
    public SVNRevision.Number getRevision() {
        return revision;
    }


    // getStatuses returns null URL for svn:externals folder.  This will
    // get the URL using svn info command on the local resource
	private String getURL(ISVNStatus status) {
		String url = status.getUrlString();
		if (url == null && info != null) {
	    	SVNUrl svnurl = info.getUrl();
	    	url = (svnurl != null) ? svnurl.toString() : null;
		}
		return url;
	}
}

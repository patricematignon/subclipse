/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.eclipse.team.svn.core.internal.status;

import org.eclipse.core.resources.IResource;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNStatus;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;

/**
 * A strategy that when asked to get the status of a given resource, 
 * get the status and the status of all its siblings. 
 * 
 * @author cedric chabanois (cchab at tigris.org) 
 */
public class NonRecursiveStatusUpdateStrategy extends StatusUpdateStrategy {

	public NonRecursiveStatusUpdateStrategy(IStatusCache statusCache)
	{
		super(statusCache);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.status.StatusUpdateStrategy#statusesToUpdate(org.eclipse.core.resources.IResource)
	 */
	protected ISVNStatus[] statusesToUpdate(IResource resource) throws SVNException {
        // we update the parent and its immediate children 
        IResource resourceToUpdate = resource;
        if ((resource.getType() == IResource.FILE)) {
            resourceToUpdate = resource.getParent();
        }
        
        if (Policy.DEBUG_STATUS) {
            System.out.println("[svn] getting status for : " + resourceToUpdate.getFullPath()); //$NON-NLS-1$   
        }
        
        // don't do getRepository().getSVNClient() as we can ask the status of a file
        // that is not associated with a known repository
        // we don't need login & password so this is not a problem
        ISVNStatus[] statuses = null;
        try {
            ISVNClientAdapter svnClientAdapterStatus = SVNProviderPlugin.getPlugin().createSVNClient();
            statuses = svnClientAdapterStatus.getStatus(
                    resourceToUpdate.getLocation().toFile(),
                    false, // do only immediate children. 
                    true); // retrieve all entries
        } catch (SVNClientException e1) {
            throw SVNException.wrapException(e1);
        }
        return statuses;
	}

}
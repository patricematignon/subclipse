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
package org.eclipse.team.svn.core.internal.status;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.ISVNStatus;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;

/**
 * A strategy that when asked to get the status of a given resource, 
 * get the status of its parent (if not present yet) and parent's children recursively
 * 
 * @author cedric chabanois (cchab at tigris.org)
 */
public class RecursiveStatusUpdateStrategy extends StatusUpdateStrategy {

	public RecursiveStatusUpdateStrategy(IStatusCache statusCache)
	{
		super(statusCache);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.status.StatusUpdateStrategy#statusesToUpdate(org.eclipse.core.resources.IResource)
	 */
	protected ISVNStatus[] statusesToUpdate(IResource resource) throws SVNException {
        if (!(resource instanceof IProject)) {
            // if the status of the resource parent is not known, we
            // recursively update it instead 
            IContainer parent = resource.getParent();
            if (parent != null) {
                if (statusCache.getStatus(parent) == null) {
                    return statusesToUpdate(parent);
                }
            }
        }
        
        if (Policy.DEBUG_STATUS) {
            System.out.println("[svn] getting status for : " + resource.getFullPath()); //$NON-NLS-1$   
        }
        
        // don't do getRepository().getSVNClient() as we can ask the status of a file
        // that is not associated with a known repository
        // we don't need login & password so this is not a problem
        ISVNStatus[] statuses = null;
        try {
            ISVNClientAdapter svnClientAdapterStatus = SVNProviderPlugin.getPlugin().createSVNClient();
            statuses = svnClientAdapterStatus.getStatus(resource.getLocation().toFile(),true, true);
        } catch (SVNClientException e1) {
            throw SVNException.wrapException(e1);
        }
        return statuses;
	}

}

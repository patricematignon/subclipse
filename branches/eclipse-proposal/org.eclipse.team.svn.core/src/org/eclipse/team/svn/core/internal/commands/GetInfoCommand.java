/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.eclipse.team.svn.core.internal.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNInfo;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;

/**
 * Get the svn info for the specified resource.
 * 
 * @author Martin Letenay (letenay at tigris.org) 
 */
public class GetInfoCommand implements ISVNCommand {

    private ISVNInfo info = null;
    private ISVNLocalResource resource = null;

    public GetInfoCommand(ISVNLocalResource resource)    
    {
    	this.resource = resource;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws SVNException {
        try {
            if (monitor != null) { monitor.beginTask(null, 100); }
            info = resource.getRepository().getSVNClient().getInfoFromWorkingCopy(resource.getFile());
            if (monitor != null) { monitor.worked(100); }
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
        	if (monitor != null) { monitor.done(); }
        }
    }
    
    public ISVNInfo getInfo() {
        return info;
    }

}
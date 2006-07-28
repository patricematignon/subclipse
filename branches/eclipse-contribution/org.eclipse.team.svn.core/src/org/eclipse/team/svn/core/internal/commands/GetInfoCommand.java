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
     * @see org.tigris.subversion.subclipse.core.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
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

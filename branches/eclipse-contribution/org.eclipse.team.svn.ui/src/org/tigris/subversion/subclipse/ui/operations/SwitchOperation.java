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
package org.tigris.subversion.subclipse.ui.operations;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.commands.SwitchToUrlCommand;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.ui.Policy;

public class SwitchOperation extends RepositoryProviderOperation {
    private SVNUrl svnUrl; 
    private SVNRevision svnRevision;
    
    public SwitchOperation(IWorkbenchPart part, IResource[] resources, SVNUrl svnUrl, SVNRevision svnRevision) {
        super(part, resources);
        this.svnUrl = svnUrl;
        this.svnRevision = svnRevision;
    }
    
    protected String getTaskName() {
        return Policy.bind("SwitchOperation.taskName"); //$NON-NLS-1$;
    }

    protected String getTaskName(SVNTeamProvider provider) {
        return Policy.bind("SwitchOperation.0", provider.getProject().getName()); //$NON-NLS-1$       
    }

    protected void execute(SVNTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws SVNException, InterruptedException {
        monitor.beginTask(null, 100);
		try {			
	    	SwitchToUrlCommand command = new SwitchToUrlCommand(provider.getSVNWorkspaceRoot(),resources[0], svnUrl, svnRevision);
	        command.run(monitor);
		} catch (SVNException e) {
		    collectStatus(e.getStatus());
		} finally {
			monitor.done();
		}  
    }

}

/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.commands;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.tigris.subversion.subclipse.core.ISVNFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.ISVNRunnable;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.client.OperationManager;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Adds a pattern to the set of ignores for the specified folder.
 * 
 * @author Cedric Chabanois (cchab at tigris.org) 
 */
public class AddIgnoredPatternCommand implements ISVNCommand {
    private ISVNLocalFolder folder;
    private String pattern;
    
    public AddIgnoredPatternCommand(ISVNLocalFolder folder, String pattern) {
        this.folder = folder;
        this.pattern = pattern;
    }
    
    
	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.core.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {
        SVNProviderPlugin.run(new ISVNRunnable() {
            public void run(IProgressMonitor monitor) throws SVNException {
                monitor.beginTask(null, 100); //$NON-NLS-1$
                if (!folder.getStatus().isManaged())
                    throw new SVNException(IStatus.ERROR, SVNException.UNABLE,
                        Policy.bind("SVNTeamProvider.ErrorSettingIgnorePattern", folder.getIResource().getFullPath().toString())); //$NON-NLS-1$
                ISVNClientAdapter svnClient = folder.getRepository().getSVNClient();
                try {
                    OperationManager.getInstance().beginOperation(svnClient);
                    
                    try {
                        svnClient.addToIgnoredPatterns(folder.getFile(), pattern);
                        
                        
                        // broadcast changes to unmanaged children - they are the only candidates for being ignored
                        ISVNResource[] members = folder.members(null, ISVNFolder.UNMANAGED_MEMBERS);
                        IResource[] possiblesIgnores = new IResource[members.length];
                        for (int i = 0; i < members.length;i++)
                            possiblesIgnores[i] = ((ISVNLocalResource)members[i]).getIResource(); 
                        folder.refreshStatus(IResource.DEPTH_ONE);
                        SVNProviderPlugin.broadcastSyncInfoChanges(possiblesIgnores);
                    }
                    catch (SVNClientException e) {
                        throw SVNException.wrapException(e);
                    }

                } finally {
                    OperationManager.getInstance().endOperation();
                    monitor.done();
                }
                
            }
        },Policy.monitorFor(monitor));
	}
    
}

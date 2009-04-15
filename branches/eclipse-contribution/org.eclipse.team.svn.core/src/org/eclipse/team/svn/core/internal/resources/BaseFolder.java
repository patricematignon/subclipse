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
package org.eclipse.team.svn.core.internal.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.ISVNResource;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNStatus;
import org.eclipse.team.svn.core.internal.commands.GetStatusCommand;

/**
 * represents the base revision of a folder
 * 
 */
public class BaseFolder extends BaseResource implements ISVNRemoteFolder {
	
	public BaseFolder(LocalResourceStatus localResourceStatus)
	{
		super(localResourceStatus);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#isContainer()
	 */
	public boolean isContainer() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNResource#isFolder()
	 */
	public boolean isFolder() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#getStorage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStorage getStorage(IProgressMonitor monitor) throws TeamException
	{	
		//Do nothing. Folders do not have contents
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNRemoteResource#members(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISVNRemoteResource[] members(IProgressMonitor progress) throws TeamException {
		return getMembers(progress);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNFolder#members(org.eclipse.core.runtime.IProgressMonitor, int)
	 */
	public ISVNResource[] members(IProgressMonitor monitor, int flags) throws SVNException {		
		final List result = new ArrayList();
		ISVNRemoteResource[] resources = getMembers(monitor);

		// RemoteFolders never have phantom members
		if ((flags & EXISTING_MEMBERS) == 0 && (flags & PHANTOM_MEMBERS) == 1) {
			return new ISVNResource[0];
		}
		boolean includeFiles = (((flags & FILE_MEMBERS) != 0) || ((flags & (FILE_MEMBERS | FOLDER_MEMBERS)) == 0));
		boolean includeFolders = (((flags & FOLDER_MEMBERS) != 0) || ((flags & (FILE_MEMBERS | FOLDER_MEMBERS)) == 0));
		boolean includeManaged = (((flags & MANAGED_MEMBERS) != 0) || ((flags & (MANAGED_MEMBERS | UNMANAGED_MEMBERS | IGNORED_MEMBERS)) == 0));
	
		for (int i = 0; i < resources.length; i++) {
			ISVNResource svnResource = resources[i];
			if ((includeFiles && ( ! svnResource.isFolder())) 
					|| (includeFolders && (svnResource.isFolder()))) {
				if (includeManaged) {
					result.add(svnResource);
				}						
			}		
		}
		return (ISVNResource[]) result.toArray(new ISVNResource[result.size()]);
	}

	/* (non-Javadoc)
     * TODO This should use the synchronization information instead of hitting the WC
	 * @see org.eclipse.team.svn.core.internal.resources.RemoteFolder#getMembers(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected ISVNRemoteResource[] getMembers(IProgressMonitor monitor)
			throws SVNException {
		final IProgressMonitor progress = Policy.monitorFor(monitor);
		progress.beginTask(Policy.bind("RemoteFolder.getMembers"), 100); //$NON-NLS-1$
        
		try {
            GetStatusCommand c = new GetStatusCommand(localResourceStatus.getRepository(), localResourceStatus.getResource(), false, true);
            c.run(monitor);
            LocalResourceStatus[] statuses = c.getLocalResourceStatuses();
            List baseChildren = new ArrayList(statuses.length);

            for (int i = 0; i < statuses.length; i++) {
                if (localResourceStatus.getFile().equals(statuses[i].getFile())) {
                    continue;
                }

                // Don't create base entries for files that aren't managed yet
                if (!statuses[i].hasRemote()) {
                    continue;
                }
                
//                // External entries don't have base information either
//                if (statuses[i].getTextStatus() == SVNStatusKind.EXTERNAL) {
//                    continue;
//                }
                
                // The folders itself is not its own child, all direct children are
                if (statuses[i].getUrlString() != null && !statuses[i].getUrlString().equals(localResourceStatus.getUrlString()))
                {
                	baseChildren.add(BaseResource.from(statuses[i]));
                }
            }
            return (ISVNRemoteResource[]) baseChildren.toArray(new ISVNRemoteResource[baseChildren.size()]);
        } catch (CoreException e)
		{
            throw new SVNException(new SVNStatus(IStatus.ERROR, SVNStatus.DOES_NOT_EXIST, Policy.bind("RemoteFolder.doesNotExist", getRepositoryRelativePath()))); //$NON-NLS-1$
        } finally {
			progress.done();
		}	 	
	}

	public void createRemoteFolder(String folderName, String message, IProgressMonitor monitor) throws SVNException {
		throw new SVNException("Cannot create remote folder on Base Folder");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNRemoteFolder#refresh()
	 */
	public void refresh() {
		//Do nothing. Base folder does NOT caches anything.
	}	
}
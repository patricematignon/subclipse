/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *
 ******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.ISVNResourceVisitor;
import org.tigris.subversion.subclipse.core.ISVNRunnable;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.commands.AddIgnoredPatternCommand;

/**
 * Implements the ISVNLocalFolder interface on top of an instance of the
 * ISVNFolder interface
 * 
 * @see ISVNLocalFolder
 */
public class LocalFolder extends LocalResource implements ISVNLocalFolder {

    /**
     * create a handle based on the given local resource.
     * Container can be IResource.ROOT
     * 
     * @param container
     */
    public LocalFolder(IContainer container) {
        super(container);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalResource#getBaseResource()
     */
    public ISVNRemoteResource getBaseResource() throws SVNException {
        if (!isManaged()) {// no base if no remote
            return null;
        }
        return new BaseFolder(getStatus());
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNFolder#members(org.eclipse.core.runtime.IProgressMonitor, int)
     */
    public ISVNResource[] members(IProgressMonitor monitor, int flags) throws SVNException {
        if (!resource.exists()) return new ISVNLocalResource[0];
        final List result = new ArrayList();
        IResource[] resources;
        try {
            resources = ((IContainer) resource).members(true);
        } catch (CoreException e) {
            throw SVNException.wrapException(e);
        }

        final boolean includeFiles = (((flags & FILE_MEMBERS) != 0) || ((flags & (FILE_MEMBERS | FOLDER_MEMBERS)) == 0));
		final boolean includeFolders = (((flags & FOLDER_MEMBERS) != 0) || ((flags & (FILE_MEMBERS | FOLDER_MEMBERS)) == 0));
		final boolean includeManaged = (((flags & MANAGED_MEMBERS) != 0) || ((flags & (MANAGED_MEMBERS
				| UNMANAGED_MEMBERS | IGNORED_MEMBERS)) == 0));
		final boolean includeUnmanaged = (((flags & UNMANAGED_MEMBERS) != 0) || ((flags & (MANAGED_MEMBERS
				| UNMANAGED_MEMBERS | IGNORED_MEMBERS)) == 0));
		final boolean includeIgnored = ((flags & IGNORED_MEMBERS) != 0);
		final boolean includeExisting = (((flags & EXISTING_MEMBERS) != 0) || ((flags & (EXISTING_MEMBERS | PHANTOM_MEMBERS)) == 0));
		final boolean includePhantoms = (((flags & PHANTOM_MEMBERS) != 0) || ((flags & (EXISTING_MEMBERS | PHANTOM_MEMBERS)) == 0));
        
        for (int i = 0; i < resources.length; i++) {
            if ((includeFiles && (resources[i].getType() == IResource.FILE))
                    || (includeFolders && (resources[i].getType() == IResource.FOLDER))) {
                ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resources[i]);
                final boolean isManaged = svnResource.isManaged();
                final boolean isIgnored = svnResource.isIgnored();
                if ((isManaged && includeManaged)
                        || (isIgnored && includeIgnored)
                        || (!isManaged && !isIgnored && includeUnmanaged)) {
                    final boolean exists = svnResource.exists();
                    if ((includeExisting && exists)
                            || (includePhantoms && !exists)) {
                        result.add(svnResource);
                    }
                }

            }
        }
        return (ISVNLocalResource[]) result
                .toArray(new ISVNLocalResource[result.size()]);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNResource#isFolder()
     */
    public boolean isFolder() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalResource#refreshStatus()
     */
    public void refreshStatus() throws SVNException {
        refreshStatus(IResource.DEPTH_ZERO);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalFolder#refreshStatus(int)
     */
    public void refreshStatus(int depth) throws SVNException {
        SVNProviderPlugin.getPlugin().getStatusCacheManager().refreshStatus(
                resource, depth);
    }

    /**
     * A folder is considered dirty if its status is dirty or if one of its children is dirty
     */
    public boolean isDirty() throws SVNException {
        if (getStatus().isDirty()) {
            return true;
        }
        
        // ignored resources are not considered dirty
        ISVNLocalResource[] children = (ISVNLocalResource[]) members(
                new NullProgressMonitor(), ALL_UNIGNORED_MEMBERS);

        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirty() || (children[i].exists() && !children[i].isManaged())) {
                // if a child resource is dirty consider the parent dirty as
                // well, there is no need to continue checking other siblings.
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalFolder#acceptChildren(org.tigris.subversion.subclipse.core.ISVNResourceVisitor)
     */
    public void acceptChildren(ISVNResourceVisitor visitor) throws SVNException {

        // Visit files and then folders
        ISVNLocalResource[] subFiles = (ISVNLocalResource[]) members(null,
                FILE_MEMBERS);
        for (int i = 0; i < subFiles.length; i++) {
            subFiles[i].accept(visitor);
        }
        ISVNLocalResource[] subFolders = (ISVNLocalResource[]) members(null,
                FOLDER_MEMBERS);
        for (int i = 0; i < subFolders.length; i++) {
            subFolders[i].accept(visitor);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalResource#accept(org.tigris.subversion.subclipse.core.ISVNResourceVisitor)
     */
    public void accept(ISVNResourceVisitor visitor) throws SVNException {
        visitor.visitFolder(this);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalFolder#unmanage(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void unmanage(IProgressMonitor monitor) throws SVNException {
        SVNProviderPlugin.run(new ISVNRunnable() {
            public void run(IProgressMonitor pm) throws SVNException {
                pm = Policy.monitorFor(pm);
                pm.beginTask(null, 100);

                ISVNResource[] members = members(Policy.subMonitorFor(pm,
                        20), FOLDER_MEMBERS | MANAGED_MEMBERS);
                ArrayList dirs = new ArrayList();
                for (int i = 0; i < members.length; i++) {
                    dirs.add(((ISVNLocalResource) members[i]).getIResource());
                }
                dirs.add(getIResource()); // we add the current folder to the
                // list : we want to delete .svn dir
                // for it too

                IProgressMonitor monitorDel = Policy.subMonitorFor(pm, 80);
                monitorDel.beginTask(null, dirs.size());

                for (int i = 0; i < dirs.size(); i++) {
                    monitorDel.worked(1);
                    IContainer container = (IContainer) dirs.get(i);
                    recursiveUnmanage(container, monitorDel);

                }
                monitorDel.done();
                pm.done();
            }

            private void recursiveUnmanage(IContainer container,
                    IProgressMonitor pm) {
                try {
                    pm.beginTask(null, 10);
                    pm.subTask(container.getFullPath().toOSString());

                    IResource[] members = container.members(true);
                    for (int i = 0; i < members.length; i++) {
                        pm.worked(1);
                        if (members[i].getType() != IResource.FILE) {
                            recursiveUnmanage((IContainer) members[i], pm);
                        }
                    }
                    // Post order traversal to make sure resources are not
                    // orphaned
                    IFolder svnFolder = container.getFolder(new Path(
                    		SVNProviderPlugin.getPlugin().getAdminDirectoryName()));
                    if (svnFolder.exists()) {
                        try {
                            svnFolder.delete(true, null);
                        } catch (CoreException e) {
                        }
                    }
                } catch (CoreException e) {
                    // Just ignore and continue
                } finally {
                    pm.done();
                }
            }
        }, Policy.subMonitorFor(monitor, 99));
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalFolder#setIgnoredAs(java.lang.String)
     */
    public void setIgnoredAs(final String pattern) throws SVNException {
        AddIgnoredPatternCommand command = new AddIgnoredPatternCommand(this, pattern);
        command.run(new NullProgressMonitor());
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalResource#revert()
     */
    public void revert() throws SVNException {
        super.revert(true);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalResource#resolve()
     */
    public void resolve() {
    	//Directories could not be resolved.
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.ISVNLocalResource#getStatus()
     */
    public LocalResourceStatus getStatus() throws SVNException {
    	if (getIResource().isTeamPrivateMember() && (SVNProviderPlugin.getPlugin().isAdminDirectory(getIResource().getName())))
    	{
    		return LocalResourceStatus.NONE;
    	}
    	return super.getStatus();
    }

}
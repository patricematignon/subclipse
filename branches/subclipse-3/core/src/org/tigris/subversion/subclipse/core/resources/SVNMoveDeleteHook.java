/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Daniel Bradby Cédric Chabanois (cchabanois@ifrance.com)
 ******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.io.File;

import org.eclipse.core.internal.resources.ResourceStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.DefaultMoveDeleteHook;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.client.OperationManager;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

public class SVNMoveDeleteHook extends DefaultMoveDeleteHook {

	public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags,
			IProgressMonitor monitor) {
System.out.println("delfile: "+file);
		ISVNLocalFile resource = new LocalFile(file);
		try {
			if (!resource.isManaged())
				return false;

			monitor.beginTask(null, 1000);
			monitor.setTaskName("Working..");
			resource.delete();
			tree.deletedFile(file);

		} catch (SVNException e) {
			tree.failed(new org.eclipse.core.runtime.Status(
					org.eclipse.core.runtime.Status.ERROR, "SUBCLIPSE", 0,
					"Error removing file", e));
			e.printStackTrace();
			return true; // we attempted
		} finally {
			monitor.done();
		}
		return true;

	}

	public boolean deleteFolder(IResourceTree tree, IFolder folder,
			int updateFlags, IProgressMonitor monitor) {
		System.out.println("delfolder: "+folder);
		ISVNLocalFolder resource = new LocalFolder(folder);

		try {
			if (!resource.isManaged())
				return false;
			monitor.beginTask(null, 1000);
			monitor.setTaskName("Working..");
			resource.delete();
			tree.deletedFolder(folder);
		} catch (SVNException e) {
			tree.failed(new org.eclipse.core.runtime.Status(
					org.eclipse.core.runtime.Status.ERROR, "SUBCLIPSE", 0,
					"Error removing folder", e));
			e.printStackTrace();
			return true; // we attempted
		} finally {
			monitor.done();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.team.IMoveDeleteHook#moveFile(org.eclipse.core.resources.team.IResourceTree,
	 *      org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile,
	 *      int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean moveFile(IResourceTree tree, IFile source,
			IFile destination, int updateFlags, IProgressMonitor monitor) {
		System.out.println("mvfile : "+source);
		try {
			ISVNLocalFile resource = new LocalFile(source);

			if (!resource.isManaged())
				return false; // pass

			ISVNClientAdapter svnClient = resource.getRepository()
					.getSVNClient();
			monitor.beginTask(null, 1000);
			monitor.setTaskName("Working..");

			boolean force = (updateFlags & IResource.FORCE) != 0;
			boolean keepHistory = (updateFlags & IResource.KEEP_HISTORY) != 0;

			// If the file is not in sync with the local file system and force
			// is false,
			// then signal that we have an error.
			if (!force
					&& !tree.isSynchronized(source, IResource.DEPTH_INFINITE)) {
				String message = org.eclipse.core.internal.utils.Policy
						.bind(
								"localstore.resourceIsOutOfSync", source.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(
						IResourceStatus.OUT_OF_SYNC_LOCAL,
						source.getFullPath(), message);
				tree.failed(status);
				return true; // we attempted even if we failed
			}

			// Add the file contents to the local history if requested by the
			// user.
			if (keepHistory)
				tree.addToLocalHistory(source);

			try {
				OperationManager.getInstance().beginOperation(svnClient);

				// add destination directory to version control if necessary
				// see bug #15
				if (!SVNWorkspaceRoot.getSVNFolderFor(destination.getParent())
						.isManaged()) {
					SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider
							.getProvider(destination.getProject());
					provider.add(new IResource[]{destination.getParent()},
							IResource.DEPTH_ZERO, new NullProgressMonitor());
				}

				// force is set to true because when we rename (refactor) a
				// java class, the file is modified before being moved
				// A modified file cannot be moved without force
				
				if(SVNWorkspaceRoot.getSVNFileFor(source).getStatus().isAdded()){
					//can't move a file that's in state added, so copy to new location
					//remove old location, add new location  
					//fix for issue 87 -mml
					source.copy(destination.getFullPath(), force, monitor);

					svnClient.addFile(destination.getLocation().toFile());
					svnClient.remove(new File[]{source.getLocation().toFile()},true);
					tree.deletedFile(source);
				}else{
					svnClient.move(source.getLocation().toFile(), destination
						.getLocation().toFile(), true);
					

				}
				 //movedFile must be done before endOperation because
				// destination file must not already exist in the workspace
				// resource tree.
				tree.movedFile(source, destination);
				destination.refreshLocal(IResource.DEPTH_ZERO, monitor);
			} catch (SVNClientException e) {
				throw SVNException.wrapException(e);
			} catch (TeamException e) {
				throw SVNException.wrapException(e);
			} catch (CoreException e) {
				throw SVNException.wrapException(e);
			} finally {
				OperationManager.getInstance().endOperation();
			}

		} catch (SVNException e) {
			tree.failed(new org.eclipse.core.runtime.Status(
					org.eclipse.core.runtime.Status.ERROR, "SUBCLIPSE", 0,
					"Error move file", e));
			e.printStackTrace();
			return true; // we attempted
		} finally {
			monitor.done();
		}
		return true;
	}

	public boolean moveFolder(IResourceTree tree, IFolder source,
			IFolder destination, int updateFlags, IProgressMonitor monitor) {
		System.out.println("mvfolder: "+source);
		try {
			ISVNLocalFolder resource = new LocalFolder(source);
			if (!resource.isManaged())
				return false;

			monitor.beginTask(null, 1000);
			monitor.setTaskName("Working..");

			// Check to see if we are synchronized with the local file system.
			// If we are in sync then we can
			// short circuit this method and do a file system only move.
			// Otherwise we have to recursively
			// try and move all resources, doing it in a best-effort manner.
			boolean force = (updateFlags & IResource.FORCE) != 0;
			if (!force
					&& !tree.isSynchronized(source, IResource.DEPTH_INFINITE)) {
				String message = org.eclipse.core.internal.utils.Policy
						.bind(
								"localstore.resourceIsOutOfSync", source.getFullPath().toString());//$NON-NLS-1$
				IStatus status = new ResourceStatus(IResourceStatus.ERROR,
						source.getFullPath(), message);
				tree.failed(status);
				return true;
			}

			ISVNClientAdapter svnClient = resource.getRepository()
					.getSVNClient();

			try {
				OperationManager.getInstance().beginOperation(svnClient);
				// add destination directory to version control if necessary
				// see bug #15
				if (!SVNWorkspaceRoot.getSVNFolderFor(destination.getParent())
						.isManaged()) {
					SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider
							.getProvider(destination.getProject());
					provider.add(new IResource[]{destination.getParent()},
							IResource.DEPTH_ZERO, new NullProgressMonitor());
				}
				
				if(SVNWorkspaceRoot.getSVNFolderFor(source).getStatus().isAdded()){
					//can't rename a folder that's in state added, so copy to new location
					//remove old location, add new location  
					//fix for issue 87 -mml
					source.copy(destination.getFullPath(), force, monitor);
					svnClient.remove(new File[]{source.getLocation().toFile()},true);
					tree.deletedFolder(source);
				}else{
				svnClient.move(source.getLocation().toFile(), destination
						.getLocation().toFile(), true);
				}

			} catch (SVNClientException e) {
				throw SVNException.wrapException(e);
			} catch (TeamException e) {
				throw SVNException.wrapException(e);
			} catch (CoreException e) {
				throw SVNException.wrapException(e);
			} finally {
				OperationManager.getInstance().endOperation();
			}
			tree.movedFolderSubtree(source, destination);
		} catch (SVNException e) {
			tree.failed(new org.eclipse.core.runtime.Status(
					org.eclipse.core.runtime.Status.ERROR, "SUBCLIPSE", 0,
					"Error move Folder " + source.getLocation(), e));
			e.printStackTrace();
			return true; // we attempted
		} finally {
			monitor.done();
		}
		return true;
	}

}
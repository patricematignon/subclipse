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

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRunnable;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.client.OperationManager;
import org.tigris.subversion.subclipse.core.resources.RemoteFolder;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Checkout the remote resources into the local workspace as projects. Each
 * resource will be checked out into the corresponding project. You can use
 * getProject to get a project for a given remote Folder
 * 
 * Resources existing in the local file system at the target project location
 * but now known to the workbench will be overwritten.
 * 
 * @author cedric chabanois (cchab at tigris.org)
 */
public class CheckoutCommand implements ISVNCommand {

	private ISVNRemoteFolder[] resources;

	private IProject[] projects;

	public CheckoutCommand(ISVNRemoteFolder[] resources, IProject[] projects) {
		this.resources = resources;
		this.projects = projects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.tigris.subversion.subclipse.core.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {
		SVNProviderPlugin.run(new ISVNRunnable() {
			public void run(IProgressMonitor pm) throws SVNException {
				pm.beginTask(null, 1000 * resources.length);

				// Get the location of the workspace root
				ISVNLocalFolder root = SVNWorkspaceRoot
						.getSVNFolderFor(ResourcesPlugin.getWorkspace()
								.getRoot());

				try {
					// Prepare the target projects to receive resources
					scrubProjects(projects, Policy.subMonitorFor(pm, 100));

					for (int i = 0; i < resources.length; i++) {
						IProject project = null;
						RemoteFolder resource = (RemoteFolder) resources[i];

						project = projects[i];
						boolean deleteDotProject = false;
						// Perform the checkout
						ISVNClientAdapter svnClient = resource.getRepository()
								.getSVNClient();

						// check if the remote project has a .project file
						ISVNDirEntry[] rootFiles = svnClient.getList(resource
								.getUrl(), SVNRevision.HEAD, false);
						for (int j = 0; j < rootFiles.length; j++) {
							if ((rootFiles[j].getNodeKind() == SVNNodeKind.FILE)
									&& (".project".equals(rootFiles[j]
											.getPath()))) {
								deleteDotProject = true;
							}
						}

						File destPath;
						if (project.getLocation() == null) {
							// project.getLocation is null if the project does
							// not exist in the workspace
							destPath = new File(root.getIResource()
									.getLocation().toFile(), project.getName());
							try {
								// we create the directory corresponding to the
								// project and we open it
								project.create(null);
                                project.open(null);
							} catch (CoreException e1) {
								throw new SVNException("Cannot create project to checkout to", e1);
							}
							

						} else {
							destPath = project.getLocation().toFile();
						}

						//delete the project file if the flag gets set.
						//fix for 54
						if (deleteDotProject) {

							IFile projectFile = project.getFile(".project");
							if (projectFile != null) {
								try {
									// delete the project file, force, no history,
									// without progress monitor
									projectFile.delete(true, false, null);
								} catch (CoreException e1) {
									throw new SVNException("Cannot delete .project before checkout",e1);
								}
							}
						}

						OperationManager operationHandler = OperationManager
								.getInstance();
						try {
							operationHandler.beginOperation(svnClient);
							svnClient.checkout(resource.getUrl(), destPath,
									SVNRevision.HEAD, true);
							pm.worked(800);
						} catch (SVNClientException e) {
							throw new SVNException("cannot checkout");
						} finally {
							operationHandler.endOperation();
						}

						// Bring the project into the workspace
						refreshProjects(new IProject[] { project }, Policy
								.subMonitorFor(pm, 100));
					} //for
                } catch (SVNClientException ce) {
					throw new SVNException("Error Getting Dir list", ce);
				} finally {
					pm.done();
				}
			} // run
		}, Policy.monitorFor(monitor));
	}

	/*
	 * Delete the target projects before checking out
	 */
	private void scrubProjects(IProject[] projects, IProgressMonitor monitor)
			throws SVNException {
		if (projects == null) {
			monitor.done();
			return;
		}
		monitor
				.beginTask(
						Policy.bind("SVNProvider.Scrubbing_projects_1"), projects.length * 100); //$NON-NLS-1$
		try {
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				if (project != null && project.exists()) {
					if (!project.isOpen()) {
						project.open(Policy.subMonitorFor(monitor, 10));
					}
					// We do not want to delete the project to avoid a project
					// deletion delta
					// We do not want to delete the .project to avoid core
					// exceptions
					monitor.subTask(Policy
							.bind("SVNProvider.Scrubbing_local_project_1")); //$NON-NLS-1$
					// unmap the project from any previous repository provider
					if (RepositoryProvider.getProvider(project) != null)
						RepositoryProvider.unmap(project);
					IResource[] children = project
							.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
					IProgressMonitor subMonitor = Policy.subMonitorFor(monitor,
							80);
					subMonitor.beginTask(null, children.length * 100);
					try {
						for (int j = 0; j < children.length; j++) {
							if (!children[j].getName().equals(".project")) {//$NON-NLS-1$
								children[j].delete(true /* force */, Policy
										.subMonitorFor(subMonitor, 100));
							}
						}
					} finally {
						subMonitor.done();
					}
				} else if (project != null) {
					// Make sure there is no directory in the local file system.
					File location = new File(project.getParent().getLocation()
							.toFile(), project.getName());
					if (location.exists()) {
						deepDelete(location);
					}
				}
			}
		} catch (CoreException e) {
			throw SVNException.wrapException(e);
		} finally {
			monitor.done();
		}
	}

	/*
	 * delete a folder recursively
	 */
	private void deepDelete(File resource) {
		if (resource.isDirectory()) {
			File[] fileList = resource.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				deepDelete(fileList[i]);
			}
		}
		resource.delete();
	}

	/*
	 * Bring the provided projects into the workspace
	 */
	private void refreshProjects(IProject[] projects, IProgressMonitor monitor)
			throws SVNException {
		monitor
				.beginTask(
						Policy.bind("SVNProvider.Creating_projects_2"), projects.length * 100); //$NON-NLS-1$
		try {
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				// Register the project with Team
				RepositoryProvider.map(project, SVNProviderPlugin.getTypeId());
				RepositoryProvider.getProvider(project, SVNProviderPlugin
						.getTypeId());
			}
		} catch (TeamException e) {
			throw new SVNException("Cannot map the project with svn provider",e);
		} finally {
			monitor.done();
		}
	}

}
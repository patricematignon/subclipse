/**********************************************************************
 Copyright (c) 2004 Dan Rubel and others.
 All rights reserved.   This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:

 Dan Rubel - initial API and implementation
 Panagiotis Korros (pkorros@bigfoot.com) - modified for Subversion
 **********************************************************************/

package org.tigris.subversion.subclipse.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.ProjectSetSerializationContext;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.commands.CheckoutCommand;
import org.tigris.subversion.subclipse.core.repo.SVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

/**
 * An object for serializing and deserializing of references to SVN based
 * projects. Given a project, it can produce a UTF-8 encoded String which can be
 * stored in a file. Given this String, it can load a project into the
 * workspace.
 */
public class SVNProjectSetCapability extends ProjectSetCapability {

	/**
	 * Override superclass implementation to return an array of project
	 * references.
	 * 
	 * @see ProjectSetSerializer#asReference(IProject[],
	 *      ProjectSetSerializationContext, IProgressMonitor)
	 */
	public String[] asReference(IProject[] projects,
			ProjectSetSerializationContext context, IProgressMonitor monitor)
			throws TeamException {

		String[] result = new String[projects.length];
		for (int i = 0; i < projects.length; i++)
			result[i] = asReference(projects[i]);
		return result;
	}

	/**
	 * Answer a string representing the specified project
	 * 
	 * @param project
	 *            the project (not <code>null</code>)
	 * @return the project reference (not <code>null</code>)
	 * @throws CVSException
	 */
	private String asReference(IProject project) throws TeamException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("0.9.3,"); //$NON-NLS-1$

		SVNTeamProvider provider = (SVNTeamProvider) RepositoryProvider
				.getProvider(project);
		SVNWorkspaceRoot root = provider.getSVNWorkspaceRoot();

		buffer.append(root.getLocalRoot().getUrl().get());
		buffer.append(",");
		buffer.append(project.getName());
		return buffer.toString();
	}

	/**
	 * Override superclass implementation to load the referenced projects into
	 * the workspace.
	 * 
	 * @see org.eclipse.team.core.ProjectSetSerializer#addToWorkspace(java.lang.String[],
	 *      org.eclipse.team.core.ProjectSetSerializationContext,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IProject[] addToWorkspace(String[] referenceStrings,
			ProjectSetSerializationContext context, IProgressMonitor monitor)
			throws TeamException {

		monitor = Policy.monitorFor(monitor);
		Policy.checkCanceled(monitor);

		// Confirm the projects to be loaded
		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = asProjects(referenceStrings, infoMap);
		projects = confirmOverwrite(context, projects);
		if (projects == null){
			return new IProject[0];
		}
		// Load the projects
		return checkout(projects, infoMap, monitor);
	}

	/**
	 * Translate the reference strings into projects to be loaded and build a
	 * mapping of project to project load information.
	 * 
	 * @param referenceStrings
	 *            project references
	 * @param infoMap
	 *            a mapping of project to project load information
	 * @return the projects to be loaded
	 */
	private IProject[] asProjects(String[] referenceStrings, Map infoMap)
			throws SVNException {
		Collection result = new ArrayList();
		for (int i = 0; i < referenceStrings.length; i++) {
			StringTokenizer tokenizer = new StringTokenizer(
					referenceStrings[i], ","); //$NON-NLS-1$
			String version = tokenizer.nextToken();
			// If this is a newer version, then ignore it
			if (!version.equals("0.9.3")){ //$NON-NLS-1$
				continue;
			}
			LoadInfo info = new LoadInfo(tokenizer);
			IProject proj = info.getProject();
			result.add(proj);
			infoMap.put(proj, info);
		}
		return (IProject[]) result.toArray(new IProject[result.size()]);
	}

	/**
	 * Checkout projects from the SVN repository
	 * 
	 * @param projects
	 *            the projects to be loaded from the repository
	 * @param infoMap
	 *            a mapping of project to project load information
	 * @param monitor
	 *            the progress monitor (not <code>null</code>)
	 */
	private IProject[] checkout(IProject[] projects, Map infoMap,
			IProgressMonitor monitor) throws TeamException {

		monitor.beginTask("", 1000 * projects.length); //$NON-NLS-1$
		List result = new ArrayList();
		try {
			for (int i = 0; i < projects.length; i++) {
				if (monitor.isCanceled()){
					break;
				}
				IProject project = projects[i];
				LoadInfo info = (LoadInfo) infoMap.get(project);
				if (info != null
						&& info.checkout(new SubProgressMonitor(monitor, 1000))){
					result.add(project);
				}
			}
		} finally {
			monitor.done();
		}
		return (IProject[]) result.toArray(new IProject[result.size()]);
	}

	/**
	 * Internal class for adding projects to the workspace
	 */
	class LoadInfo {
		private final ISVNRepositoryLocation repositoryLocation;
		private final IProject project;

		/**
		 * Construct a new instance wrappering the specified project reference
		 * 
		 * @param projRef
		 *            the project reference
		 */
		LoadInfo(StringTokenizer tokenizer) throws SVNException {
			String repo = tokenizer.nextToken();
			repositoryLocation = SVNRepositoryLocation.fromString(repo);
			String projectName = tokenizer.nextToken();
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(
					projectName);
		}

		/**
		 * Answer the project referenced by this object. The project may or may
		 * not already exist.
		 * 
		 * @return the project (not <code>null</code>)
		 */
		private IProject getProject() {
			return project;
		}

		/**
		 * Checkout the project specified by this reference.
		 * 
		 * @param monitor
		 *            project monitor
		 * @return true if loaded, else false
		 * @throws TeamException
		 */
		boolean checkout(IProgressMonitor monitor) throws TeamException {
			if (repositoryLocation == null){
				return false;
			}
            CheckoutCommand command = new CheckoutCommand(new ISVNRemoteFolder[]{(ISVNRemoteFolder) repositoryLocation
                    .getRootFolder()}, new IProject[]{project});
            command.run(monitor);
			return true;
		}
	}

}
/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.operations.ShowAnnotationOperation;

public class ShowAnnotationAction extends WorkspaceAction {

	/**
	 * Action to open a SVN Annotate View
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		// Get the selected resource.
		final ISVNRemoteFile cvsResource = getSingleSelectedSVNRemoteFile();
		execute(cvsResource);
	}

	public void execute(final ISVNRemoteFile svnResource) throws InvocationTargetException, InterruptedException {

		if (svnResource == null) {
			return;
		}

        new ShowAnnotationOperation(getTargetPart(), svnResource).run();

	}
	
	/**
	 * Ony enabled for single resource selection
	 */
	protected boolean isEnabled() throws TeamException {
		ISVNRemoteFile resource = getSingleSelectedSVNRemoteFile();
		return (resource != null);
	}

	/**
	 * This action is called from one of a Resource Navigator a SVN Resource
	 * Navigator or a History Log Viewer. Return the selected resource as an
	 * ISVNRemoteFile
	 * 
	 * @return ICVSResource
	 * @throws SVNException
	 */
	protected ISVNRemoteFile getSingleSelectedSVNRemoteFile() {
		// Selected from a SVN Resource Navigator or History
		ISVNRemoteFile[] svnResources = this.getSelectedRemoteFiles();
		if (svnResources.length == 1) {
			return svnResources[0];
		}

		// Selected from a Resource Navigator
		IResource[] resources = getSelectedResources();
		if (resources.length == 1) {
			try {
				return (ISVNRemoteFile)SVNWorkspaceRoot.getBaseResourceFor(resources[0]);
			} catch (SVNException e) {
				return null;
			}
		}
		return null;
	}
}

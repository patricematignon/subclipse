/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.actions;
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.dialogs.IPromptCondition;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class ReplaceWithRemoteAction extends WorkspaceAction {
	public void execute(IAction action)  throws InvocationTargetException, InterruptedException {
		
		final IResource[][] resources = new IResource[][] {null};

		try {
			resources[0] = checkOverwriteOfDirtyResources(getSelectedResources());
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		}
			
		if (resources[0] == null || resources[0].length == 0) return;
		
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					monitor.beginTask(Policy.bind("ReplaceWithRemoteAction.replacing"), resources[0].length * 100);
					// Peform the replace in the background
					//		new ReplaceOperation(getTargetPart(), resources[0], null, true).run();
					for (int i = 0; i < resources[0].length; i++) {
						if (monitor.isCanceled()) {
							return;
						}
						
						IResource resource = resources[0][i];
						ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
						if (localResource.isModified()) {
							localResource.revert();
						}
						
						SVNTeamProvider provider = (SVNTeamProvider)RepositoryProvider.getProvider(resource.getProject());
						provider.update(new IResource[] {resource}, SVNRevision.HEAD, new SubProgressMonitor(monitor, 100));	
						
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
		
	}
	
	protected IPromptCondition getPromptCondition(IResource[] dirtyResources) {
		return getOverwriteLocalChangesPrompt(dirtyResources);
	}	
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("ReplaceWithRemoteAction.problemMessage"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}

}

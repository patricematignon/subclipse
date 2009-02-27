/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.actions;
 
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.compare.SVNLocalCompareInput;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public abstract class CompareWithRemoteAction extends WorkspaceAction {

	private final SVNRevision revision;

	/**
	 * Creates a new CompareWithRemoteAction for the specified revision
	 * @param revision Revision to compare against.  Only relative resisions (HEAD,BASE,PREVIOUS) shoudl be used
	 */
	public CompareWithRemoteAction(SVNRevision revision) {
		this.revision = revision;
	}

	public void execute(IAction action) {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return;
		
		try {
			final ISVNLocalResource localResource= SVNWorkspaceRoot.getSVNResourceFor(resources[0]);
			
			run(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					try {
						CompareUI.openCompareEditorOnPage(
								new SVNLocalCompareInput(localResource, revision),
								getTargetPage());
					} catch (SVNException e) {
						handle(e, null, null);
					}
				}
			}, false /* cancelable */, PROGRESS_BUSYCURSOR);
		} catch (Exception e) {
			handle(e, null, null);
		}
		
		
	}
	
	/*
	 * Update the text label for the action based on the tags in the selection.
	 * @see TeamAction#setActionEnablement(org.eclipse.jface.action.IAction)
	 */
	protected void setActionEnablement(IAction action) {
		super.setActionEnablement(action);
		
	}
	
	/**
	 * Enable for resources that are managed (using super) or whose parent is an SVN folder.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ISVNLocalResource cvsResource) throws SVNException {
		return super.isEnabledForSVNResource(cvsResource) || cvsResource.getParent().isManaged();
	}

	/**
	 * Added resources don't have any details to compare against
	 * TODO if the addition is because of a copy this should be allowed (requires some way to get the remote resource from the original location)
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}
}
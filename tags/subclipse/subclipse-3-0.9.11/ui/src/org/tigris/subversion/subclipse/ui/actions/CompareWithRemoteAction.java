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

public class CompareWithRemoteAction extends WorkspaceAction {

	public void execute(IAction action) {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return;
		
		try {
			final ISVNLocalResource localResource= SVNWorkspaceRoot.getSVNResourceFor(resources[0]);
			
			run(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					try {
						CompareUI.openCompareEditorOnPage(
								new SVNLocalCompareInput(localResource, SVNRevision.HEAD),
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
	 * Enable for resources that are managed (using super) or whose parent is a CVS folder.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ISVNLocalResource cvsResource) throws SVNException {
		return super.isEnabledForSVNResource(cvsResource) || cvsResource.getParent().isManaged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
}

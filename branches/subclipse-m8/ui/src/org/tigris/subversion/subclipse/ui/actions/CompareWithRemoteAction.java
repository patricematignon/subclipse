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
 
import org.eclipse.jface.action.IAction;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.ui.Policy;

public class CompareWithRemoteAction extends WorkspaceAction {

	public void execute(IAction action) {
		
		
		//TODO: this should probably do something too. see cvs version
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CompareWithRemoteAction.compare"); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForSVNResource(ISVNLocalResource svnResource) throws SVNException {
		return super.isEnabledForSVNResource(svnResource);
        /*
        if (super.isEnabledForSVNResource(svnResource)) {
			// Don't enable if there are sticky file revisions in the lineup
			if (!cvsResource.isFolder()) {
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				if (info != null && info.getTag() != null) {
					String revision = info.getRevision();
					String tag = info.getTag().getName();
					if (revision.equals(tag)) return false;
				}
			}
			return true;
		} else {
			return getTag(cvsResource) != null;
		} */
	}
	
	/*
	 * Update the text label for the action based on the tags in the
	 * selection.
	 * @see TeamAction#setActionEnablement(org.eclipse.jface.action.IAction)
	 */
/*	protected void setActionEnablement(IAction action) {
		super.setActionEnablement(action);
		action.setText(calculateActionTagValue());
	}
*/
}

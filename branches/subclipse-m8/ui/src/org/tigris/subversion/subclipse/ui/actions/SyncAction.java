/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
 
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.subscriber.SVNWorkspaceSynchronizeParticipant;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncAction extends WorkspaceAction {
	
	public void execute(IAction action) {
		IResource[] resources = getResourcesToSync();
		if (resources == null || resources.length == 0) return;
		
		SVNWorkspaceSynchronizeParticipant participant = SVNUIPlugin.getPlugin().getSVNWorkspaceSynchronizeParticipant();
		if(participant != null) {
			IWizard wizard = participant.createSynchronizeWizard();
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			dialog.open();
		}
	}
	
	protected IResource[] getResourcesToSync() {
		return getSelectedResources();
	}
	
	
	
	
	/**
	 * Enable for resources that are managed (using super) or whose parent is a
	 * SVN folder.
	 * 
	 * @see org.tigris.subversion.subclipse.ui.actions.WorkspaceAction#isEnabledForSVNResource(org.tigris.subversion.subclipse.core.ISVNResource)
	 */
	protected boolean isEnabledForSVNResource(ISVNLocalResource svnResource) throws SVNException {
		return super.isEnabledForSVNResource(svnResource) || svnResource.getParent().isManaged();
	}

}

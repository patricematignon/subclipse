/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.ui.wizards.NewRemoteFolderWizard;

/**
 * Action to create a remote folder on repository
 */
public class CreateRemoteFolderAction extends SVNAction {

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.ui.actions.SVNAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action)throws InvocationTargetException, InterruptedException {
          
        ISVNRemoteFolder remoteFolder = null;
                 
        if (selection.getFirstElement() instanceof ISVNRemoteFolder)
            remoteFolder = (ISVNRemoteFolder)selection.getFirstElement();
        else
        if (selection.getFirstElement() instanceof ISVNRepositoryLocation)
            remoteFolder = ((ISVNRepositoryLocation)selection.getFirstElement()).getRootFolder();
        else
        if (selection.getFirstElement() instanceof ISVNRemoteFile)
            remoteFolder = ((ISVNRemoteFile)selection.getFirstElement()).getParent();
                
        NewRemoteFolderWizard wizard = new NewRemoteFolderWizard(remoteFolder);
                
        WizardDialog dialog = new WizardDialog(shell, wizard);
        wizard.setParentDialog(dialog);
        dialog.open();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return (selection.size() == 1);
	}

}

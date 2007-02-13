/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;
import org.eclipse.team.svn.ui.internal.wizards.NewRemoteFolderWizard;

/**
 * Action to create a remote folder on repository
 */
public class CreateRemoteFolderAction extends SVNAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action){
          
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
	protected boolean isEnabled(){
		return (selection.size() == 1);
	}

}

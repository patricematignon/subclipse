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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.ui.internal.WorkspacePathValidator;
import org.eclipse.team.svn.ui.internal.wizards.CheckoutWizard;

/**
 * Add a remote resource to the workspace. Current implementation:
 * - Works only for remote folders
 * - prompt for project name
 */
public class CheckoutAsAction extends SVNAction {

    /*
     * @see TeamAction#isEnabled()
     */
    protected boolean isEnabled() {
//        return getSelectedRemoteFolders().length == 1;
    	return getSelectedRemoteFolders().length > 0;
    }

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		if (!WorkspacePathValidator.validateWorkspacePath()) return;
	    final ISVNRemoteFolder[] folders = getSelectedRemoteFolders();
	    
	    CheckoutWizard wizard = new CheckoutWizard(folders);
	    WizardDialog dialog = new WizardDialog(shell, wizard);
	    dialog.open();
	}

}
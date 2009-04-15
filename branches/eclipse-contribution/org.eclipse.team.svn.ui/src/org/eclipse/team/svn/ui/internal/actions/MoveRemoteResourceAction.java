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
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.ui.internal.wizards.MoveRemoteResourceWizard;

/**
 * Action to move a remote resource on repository
 */
public class MoveRemoteResourceAction extends SVNAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.actions.SVNAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action) {
          
 
        MoveRemoteResourceWizard wizard = new MoveRemoteResourceWizard((ISVNRemoteResource)selection.getFirstElement());
                
        WizardDialog dialog = new WizardDialog(shell, wizard);
        wizard.setParentDialog(dialog);
        dialog.open();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		return (selection.size() == 1) && (selection.getFirstElement() instanceof ISVNRemoteResource); 
	}

}
/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.ui.internal.dialogs.ShowDifferencesAsUnifiedDiffDialogWC;

public class ShowDifferencesAsUnifiedDiffActionWC extends WorkspaceAction {

	public ShowDifferencesAsUnifiedDiffActionWC() {
		super();
	}

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		IResource[] resources = getSelectedResources();
		ShowDifferencesAsUnifiedDiffDialogWC dialog = new ShowDifferencesAsUnifiedDiffDialogWC(getShell(), resources[0], getTargetPart());
		dialog.open();
	}

	protected boolean isEnabled() throws TeamException {
		return getSelectedResources().length == 1;
	}

}
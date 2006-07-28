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
package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.tigris.subversion.subclipse.ui.dialogs.ShowDifferencesAsUnifiedDiffDialog;

public class ShowDifferencesAsUnifiedDiffAction extends SVNAction {

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		ISVNRemoteResource[] selectedResources = getSelectedRemoteResources();
		ShowDifferencesAsUnifiedDiffDialog dialog = new ShowDifferencesAsUnifiedDiffDialog(getShell(), selectedResources, getTargetPart());
		dialog.open();
	}

	protected boolean isEnabled() throws TeamException {
		ISVNRemoteResource[] selectedResources = getSelectedRemoteResources();
		if (selectedResources.length != 2) return false;
		if (!selectedResources[0].getRepository().getRepositoryRoot().toString().equals(selectedResources[1].getRepository().getRepositoryRoot().toString())) return false;
		if (selectedResources[0] instanceof ISVNRemoteFolder && selectedResources[1] instanceof ISVNRemoteFolder) return true;
		if (selectedResources[0] instanceof ISVNRemoteFile && selectedResources[1] instanceof ISVNRemoteFile) return true;
		return false;
	}

}

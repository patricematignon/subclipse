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
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.ui.dialogs.RemoteResourcePropertiesDialog;

public class RemoteResourcePropertiesAction extends SVNAction {

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		ISVNRemoteResource[] remoteResources = getSelectedRemoteResources();
		RemoteResourcePropertiesDialog dialog = new RemoteResourcePropertiesDialog(getShell(), remoteResources[0]);
		dialog.open();
	} 

	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteResources().length == 1;
	}

}

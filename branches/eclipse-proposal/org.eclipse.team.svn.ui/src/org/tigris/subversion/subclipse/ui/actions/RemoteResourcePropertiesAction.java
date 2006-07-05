package org.tigris.subversion.subclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
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

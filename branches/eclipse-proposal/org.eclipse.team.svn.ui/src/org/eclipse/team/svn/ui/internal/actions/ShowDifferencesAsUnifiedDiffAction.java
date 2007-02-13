package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.ui.internal.dialogs.ShowDifferencesAsUnifiedDiffDialog;

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

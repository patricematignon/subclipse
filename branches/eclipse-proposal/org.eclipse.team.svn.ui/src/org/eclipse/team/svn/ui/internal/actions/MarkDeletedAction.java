package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.operations.RemoveOperation;

public class MarkDeletedAction extends WorkspaceAction {

	public MarkDeletedAction() {
		super();
	}

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		RemoveOperation removeOperation = new RemoveOperation(getTargetPart(), getSelectedResources());
		removeOperation.run();
	}

	protected boolean isEnabled() throws TeamException {
		boolean enabled = super.isEnabled();
		if (!enabled) return false;
		IResource[] resources = getSelectedResources();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.exists()) return false;
			ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
			if (!svnResource.getStatus().isMissing()) return false;
		}
		return true;
	}

	protected boolean isEnabledForInaccessibleResources() {
		return true;
	}

	protected boolean isEnabledForAddedResources() {
		return false;
	}

}
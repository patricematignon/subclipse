package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.repo.SVNRepositoryLocation;
import org.eclipse.team.svn.ui.internal.wizards.RelocateWizard;

public class RelocateAction extends SVNAction {

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		Iterator iter = selection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SVNRepositoryLocation) {
				SVNRepositoryLocation repository = (SVNRepositoryLocation)object;
				RelocateWizard wizard = new RelocateWizard(repository);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
				break;
			}
		}
	}

	protected boolean isEnabled() throws TeamException {
		return selection.size() == 1;
	}

}

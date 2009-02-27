package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.operations.ExportOperation;

public class ExportAction extends WorkspaceAction {

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
		dialog.setText(Policy.bind("ExportAction.exportTo"));
		String directory = dialog.open();
		if (directory == null) return;
		new ExportOperation(getTargetPart(), getSelectedResources(), directory).run();
	}

}
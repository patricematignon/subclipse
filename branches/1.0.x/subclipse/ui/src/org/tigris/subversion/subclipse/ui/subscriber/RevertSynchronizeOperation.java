package org.tigris.subversion.subclipse.ui.subscriber;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.ui.dialogs.RevertDialog;
import org.tigris.subversion.subclipse.ui.operations.RevertOperation;

public class RevertSynchronizeOperation extends SVNSynchronizeOperation {
	private String url;
	private IResource[] resources;
	private IResource[] resourcesToRevert;
	private boolean revert;

	public RevertSynchronizeOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements, String url, IResource[] resources) {
		super(configuration, elements);
		this.url = url;
		this.resources = resources;
	}

	protected boolean promptForConflictHandling(Shell shell, SyncInfoSet syncSet) {
		return true;
	}

	protected void run(SVNTeamProvider provider, SyncInfoSet set, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				if (resources == null || resources.length == 0) {
					revert = false;
					return;
				}
				RevertDialog dialog = new RevertDialog(getShell(), resources, url);
				revert = (dialog.open() == RevertDialog.OK);
				if (revert) resourcesToRevert = dialog.getSelectedResources();
			}
		});
		if (revert) new RevertOperation(getPart(), resourcesToRevert).run();
	}

}

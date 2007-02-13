package org.eclipse.team.svn.ui.internal.operations;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.commands.ImportCommand;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.IWorkbenchPart;

public class ImportOperation extends SVNOperation {
	private File directory;
	private ISVNRemoteFolder folder;
	private String commitComment;
	private boolean recurse;

	public ImportOperation(IWorkbenchPart part, ISVNRemoteFolder folder, File directory, String comment, boolean recurse) {
		super(part);
		this.folder = folder;
		this.directory = directory;
		this.commitComment = comment;
		this.recurse = recurse;
	}
	
	protected String getTaskName() {
		return Policy.bind("ImportOperation.taskName"); //$NON-NLS-1$;
	}

	protected String getTaskName(SVNTeamProvider provider) {
		return Policy.bind("ImportOperation.0", directory.toString()); //$NON-NLS-1$  		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.ui.internal.operations.SVNOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws SVNException,
			InterruptedException {

	    monitor.beginTask(null, 100);
		try {
		    ImportCommand command = new ImportCommand(folder, directory, commitComment, recurse);
	        command.run(monitor);
		} catch (SVNException e) {
		    collectStatus(e.getStatus());
		} finally {
			monitor.done();
		}         

	}
}

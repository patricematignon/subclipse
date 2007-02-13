package org.eclipse.team.svn.ui.internal.operations;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.IWorkbenchPart;

public class ExportRemoteFolderOperation extends SVNOperation {
	private ISVNRemoteFolder folder;
	private File directory;
	private SVNRevision revision;

	public ExportRemoteFolderOperation(IWorkbenchPart part, ISVNRemoteFolder folder, File directory, SVNRevision revision) {
		super(part);
		this.folder = folder;
		this.directory = directory;
		this.revision = revision;
	}
	
	protected String getTaskName() {
		return Policy.bind("ExportOperation.taskName"); //$NON-NLS-1$;
	}

	protected String getTaskName(SVNTeamProvider provider) {
		return Policy.bind("ExportOperation.0", provider.getProject().getName()); //$NON-NLS-1$  		
	}

	protected void execute(IProgressMonitor monitor) throws SVNException, InterruptedException {
		try {
			ISVNClientAdapter client = folder.getRepository().getSVNClient();
			try {
				client.doExport(folder.getUrl(), directory, revision, true);
			} catch (SVNClientException e) {
				throw SVNException.wrapException(e);
			}
		} catch (SVNException e) {
		    collectStatus(e.getStatus());
		} finally {
			monitor.done();
		}         
	}

}

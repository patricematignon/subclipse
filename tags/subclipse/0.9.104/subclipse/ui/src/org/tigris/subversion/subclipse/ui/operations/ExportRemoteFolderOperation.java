package org.tigris.subversion.subclipse.ui.operations;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

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

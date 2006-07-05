package org.tigris.subversion.subclipse.ui.operations;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.ui.Policy;

public class ShowDifferencesAsUnifiedDiffOperation extends SVNOperation {
	private SVNUrl fromUrl;
	private SVNRevision fromRevision;
	private SVNUrl toUrl;
	private SVNRevision toRevision;
	private File file;

	public ShowDifferencesAsUnifiedDiffOperation(IWorkbenchPart part, SVNUrl fromUrl, SVNRevision fromRevision, SVNUrl toUrl, SVNRevision toRevision, File file) {
		super(part);
		this.fromUrl = fromUrl;
		this.toUrl = toUrl;
		this.fromRevision = fromRevision;
		this.toRevision = toRevision;
		this.file = file;
	}

	protected void execute(IProgressMonitor monitor) throws SVNException, InterruptedException {
		ISVNClientAdapter client = null;
		ISVNRepositoryLocation repository = SVNProviderPlugin.getPlugin().getRepository(fromUrl.toString());
		if (repository != null)
			client = repository.getSVNClient();
		if (client == null)
			client = SVNProviderPlugin.getPlugin().getSVNClientManager().createSVNClient();
		try {
			client.diff(fromUrl, fromRevision, toUrl, toRevision, file, true);
		} catch (SVNClientException e) {
			throw SVNException.wrapException(e);
		} finally {
			monitor.done();
		}      
	}

	protected String getTaskName() {
		return Policy.bind("HistoryView.showDifferences"); //$NON-NLS-1$
	}

}

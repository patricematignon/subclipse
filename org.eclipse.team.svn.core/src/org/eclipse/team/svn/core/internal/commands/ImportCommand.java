package org.eclipse.team.svn.core.internal.commands;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.client.OperationProgressNotifyListener;

/**
 * Import local folder to repository
 */
public class ImportCommand implements ISVNCommand {

    private ISVNRemoteFolder folder;
    private File dir;
    String comment;
    boolean recurse;
    

    public ImportCommand(ISVNRemoteFolder folder, File dir, String comment, boolean recurse) {
        super();
        this.folder = folder;
        this.dir = dir;
        this.comment = comment;
        this.recurse = recurse;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {       
		final IProgressMonitor subPm = Policy.infiniteSubMonitorFor(monitor, 100);
        try {
    		subPm.beginTask(null, Policy.INFINITE_PM_GUESS_FOR_SWITCH);
            ISVNClientAdapter svnClient = folder.getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient, new OperationProgressNotifyListener(subPm));
			svnClient.doImport(dir, folder.getUrl(), comment, recurse);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            OperationManager.getInstance().endOperation();
            subPm.done();
        }
	}

}

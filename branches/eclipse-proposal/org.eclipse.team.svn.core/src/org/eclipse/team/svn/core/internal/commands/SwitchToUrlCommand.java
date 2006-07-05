package org.eclipse.team.svn.core.internal.commands;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.core.internal.Policy;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.client.OperationProgressNotifyListener;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;

/**
 * Switch URL for selected resource
 */
public class SwitchToUrlCommand implements ISVNCommand {
	// resource to switch
    private IResource resource;  
    
    private SVNUrl svnUrl;
    
    private SVNRevision svnRevision;
    
    private SVNWorkspaceRoot root;

    public SwitchToUrlCommand(SVNWorkspaceRoot root, IResource resource, SVNUrl svnUrl, SVNRevision svnRevision) {
        super();
        this.root = root;
        this.resource = resource;
        this.svnUrl = svnUrl;
        this.svnRevision = svnRevision;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {       
		final IProgressMonitor subPm = Policy.infiniteSubMonitorFor(monitor, 100);
        try {
    		subPm.beginTask(null, Policy.INFINITE_PM_GUESS_FOR_SWITCH);
            ISVNClientAdapter svnClient = root.getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient, new OperationProgressNotifyListener(subPm));
            File file = resource.getLocation().toFile();
            svnClient.switchToUrl(file, svnUrl, svnRevision, true);
            try {
                // Refresh the resource after merge
                resource.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            } catch (CoreException e1) {
            }
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            OperationManager.getInstance().endOperation();
            subPm.done();
        }
	}

}

package org.tigris.subversion.subclipse.core.commands;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.client.OperationManager;
import org.tigris.subversion.subclipse.core.client.OperationProgressNotifyListener;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

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
	 * @see org.tigris.subversion.subclipse.core.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws SVNException {       
		final IProgressMonitor subPm = Policy.infiniteSubMonitorFor(monitor, 100);
        try {
    		subPm.beginTask(null, Policy.INFINITE_PM_GUESS_FOR_SWITCH);
            ISVNClientAdapter svnClient = root.getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient, new OperationProgressNotifyListener(subPm));
            File file = resource.getLocation().toFile();
            svnClient.switchToUrl(file, svnUrl, svnRevision, true);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            OperationManager.getInstance().endOperation();
            subPm.done();
        }
	}

}

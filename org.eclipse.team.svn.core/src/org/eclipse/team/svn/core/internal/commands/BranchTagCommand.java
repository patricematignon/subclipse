package org.eclipse.team.svn.core.internal.commands;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.client.OperationManager;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;

public class BranchTagCommand implements ISVNCommand {
	// selected resource
    private IResource resource; 
    
    private SVNUrl sourceUrl;
    private SVNUrl destinationUrl;
    private boolean createOnServer;
    private String message;
    private SVNRevision revision;
    
    private SVNWorkspaceRoot root;

    public BranchTagCommand(SVNWorkspaceRoot root, IResource resource, SVNUrl sourceUrl, SVNUrl destinationUrl, String message, boolean createOnServer, SVNRevision revision) {
        super();
        this.root = root;
        this.resource = resource;
        this.sourceUrl = sourceUrl;
        this.destinationUrl = destinationUrl;
        this.createOnServer = createOnServer;
        this.message = message;
        this.revision = revision;
    }

    public void run(IProgressMonitor monitor) throws SVNException {
        try {
            monitor.beginTask(null, 100);
            ISVNClientAdapter svnClient = root.getRepository().getSVNClient();
            OperationManager.getInstance().beginOperation(svnClient);
            monitor.subTask(resource.getName());
            if (createOnServer) svnClient.copy(sourceUrl, destinationUrl, message, revision);
            else {
                File file = resource.getLocation().toFile();
                svnClient.copy(file, destinationUrl, message);
            }
            monitor.worked(100);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            OperationManager.getInstance().endOperation();
            monitor.done();
        }                
    }

}

package org.tigris.subversion.subclipse.core.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNAnnotations;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNRevision;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.SVNException;

public class GetAnnotationsCommand implements ISVNCommand {

    private ISVNAnnotations annotations;
    private final SVNRevision fromRevision;
    private final SVNRevision toRevision;
    private final ISVNRemoteFile remoteFile;
    
    public GetAnnotationsCommand(ISVNRemoteFile remoteFile, SVNRevision fromRevision, SVNRevision toRevision) {
        this.remoteFile = remoteFile;
        this.fromRevision = fromRevision;
        this.toRevision = toRevision;
    }
    
    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws SVNException {
        try {
            monitor.beginTask(null, 100);
            annotations = remoteFile.getRepository().getSVNClient().annotate(remoteFile.getUrl(), fromRevision, toRevision);
//            annotations = remoteFile.getAnnotations(monitor);
            monitor.worked(100);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            monitor.done();
        }
    }
    
    public ISVNAnnotations getAnnotations() {
        return annotations;
    }

}

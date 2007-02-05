package org.tigris.subversion.subclipse.core.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Get the svn blame for the specified resource 
 *
 * @author Martin
 */
public class GetAnnotationsCommand implements ISVNCommand {

    private ISVNAnnotations annotations;
    private final SVNRevision fromRevision;
    private final SVNRevision toRevision;
    private final ISVNRemoteFile remoteFile;
  
    /**
     * Constructor
     * @param remoteFile
     * @param fromRevision
     * @param toRevision
     */
    public GetAnnotationsCommand(ISVNRemoteFile remoteFile, SVNRevision fromRevision, SVNRevision toRevision) {
        this.remoteFile = remoteFile;
        this.fromRevision = fromRevision;
        this.toRevision = toRevision;
    }
    
    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.core.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor aMonitor) throws SVNException {
		IProgressMonitor monitor = Policy.monitorFor(aMonitor);
		monitor.beginTask(Policy.bind("RemoteFile.getAnnotations"), 100);//$NON-NLS-1$
        try {        	
            annotations = remoteFile.getAnnotations(fromRevision, toRevision);
            monitor.worked(100);
        } catch (TeamException e) {
            throw SVNException.wrapException(e);
        } finally {
            monitor.done();
        }
    }
    
    /**
     * @return the annotations retrieved for the specified resource
     */
    public ISVNAnnotations getAnnotations() {
        return annotations;
    }

}

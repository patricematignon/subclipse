/*
 * Created on 29 ��� 2004
 */
package org.tigris.subversion.subclipse.ui.operations;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.commands.UpdateResourcesCommand;
import org.eclipse.team.svn.core.internal.sync.SVNWorkspaceSubscriber;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.ui.Policy;

/**
 * @author Panagiotis K
 */
public class UpdateOperation extends RepositoryProviderOperation {
	private final SVNRevision revision;
	private final boolean recursive; 

    /**
     * @param part
     * @param resources
     */
    public UpdateOperation(IWorkbenchPart part, IResource[] resources, SVNRevision revision, boolean recursive) {
        super(part, resources);
        this.revision = revision;
        this.recursive = recursive;
    }

    public UpdateOperation(IWorkbenchPart part, IResource resource, SVNRevision revision) {
        super(part, new IResource[] {resource});
        this.revision = revision;
        this.recursive = false;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("UpdateOperation.taskName"); //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected String getTaskName(SVNTeamProvider provider) {
		return Policy.bind("UpdateOperation.0", provider.getProject().getName()); //$NON-NLS-1$
	}


    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.ui.operations.RepositoryProviderOperation#execute(org.eclipse.team.svn.core.internal.SVNTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void execute(SVNTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws SVNException, InterruptedException {
        monitor.beginTask(null, 100);
		try {			
		    SVNWorkspaceSubscriber.getInstance().updateRemote(resources);
	    	UpdateResourcesCommand command = new UpdateResourcesCommand(provider.getSVNWorkspaceRoot(),resources, revision, recursive);
	        command.run(Policy.subMonitorFor(monitor,100));
			//updateWorkspaceSubscriber(provider, resources, Policy.subMonitorFor(monitor, 5));
		} catch (SVNException e) {
		    collectStatus(e.getStatus());
		} catch (TeamException e) {
		    collectStatus(e.getStatus());
        } finally {
            monitor.done();
		}
    }
}

package org.eclipse.team.svn.ui.internal.operations;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.commands.ResolveResourcesCommand;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.IWorkbenchPart;

public class ResolveOperation extends RepositoryProviderOperation {

    public ResolveOperation(IWorkbenchPart part, IResource[] resources) {
        super(part, resources);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
     */
    protected String getTaskName() {
        return Policy.bind("ResolveOperation.taskName"); //$NON-NLS-1$;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
     */
    protected String getTaskName(SVNTeamProvider provider) {
        return Policy.bind("ResolveOperation.0", provider.getProject().getName()); //$NON-NLS-1$
    }


    /* (non-Javadoc)
     * @see org.eclipse.team.svn.ui.internal.operations.RepositoryProviderOperation#execute(org.eclipse.team.svn.core.internal.SVNTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void execute(SVNTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws SVNException, InterruptedException {
        monitor.beginTask(null, 100);
        try {           
            ResolveResourcesCommand command = new ResolveResourcesCommand(provider.getSVNWorkspaceRoot(),resources);
            command.run(Policy.subMonitorFor(monitor,100));
        } catch (SVNException e) {
            collectStatus(e.getStatus());
        } finally {
            monitor.done();
        }
    }
}

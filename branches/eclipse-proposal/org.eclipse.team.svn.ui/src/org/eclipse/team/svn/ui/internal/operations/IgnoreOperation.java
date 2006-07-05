package org.eclipse.team.svn.ui.internal.operations;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.commands.AddIgnoredPatternCommand;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.dialogs.IgnoreResourcesDialog;
import org.eclipse.ui.IWorkbenchPart;

public class IgnoreOperation extends RepositoryProviderOperation {

    private final IgnoreResourcesDialog dialog;


    public IgnoreOperation(IWorkbenchPart part, IResource[] resources, IgnoreResourcesDialog dialog) {
        super(part, resources);
        this.dialog = dialog;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
     */
    protected String getTaskName() {
        return Policy.bind("IgnoreOperation.taskName"); //$NON-NLS-1$;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
     */
    protected String getTaskName(SVNTeamProvider provider) {
        return Policy.bind("IgnoreOperation.0", provider.getProject().getName()); //$NON-NLS-1$
    }


    /* (non-Javadoc)
     * @see org.eclipse.team.svn.ui.internal.operations.RepositoryProviderOperation#execute(org.eclipse.team.svn.core.internal.SVNTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void execute(SVNTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws SVNException, InterruptedException {
        try {
            for (int i = 0; i < resources.length; i++) {
                IResource resource = resources[i];
                String pattern = dialog.getIgnorePatternFor(resource);
                ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                new AddIgnoredPatternCommand(svnResource.getParent(), pattern).run(monitor);
            }
        } catch (SVNException e) {
            collectStatus(e.getStatus());
        } finally {
            monitor.done();
        }
    }
}

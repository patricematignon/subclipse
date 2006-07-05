package org.eclipse.team.svn.ui.internal.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.commands.UnlockResourcesCommand;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class UnlockAction extends WorkspaceAction {

    protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
        final IResource[] resources = getSelectedResources(); 
        run(new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
                try {
					Hashtable table = getProviderMapping(getSelectedResources());
					Set keySet = table.keySet();
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {
					    SVNTeamProvider provider = (SVNTeamProvider)iterator.next();
				    	UnlockResourcesCommand command = new UnlockResourcesCommand(provider.getSVNWorkspaceRoot(), resources, false);
				        command.run(Policy.subMonitorFor(monitor,1000));    					
					}
                } catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
            }              
        }, true /* cancelable */, PROGRESS_DIALOG);        
    }

}

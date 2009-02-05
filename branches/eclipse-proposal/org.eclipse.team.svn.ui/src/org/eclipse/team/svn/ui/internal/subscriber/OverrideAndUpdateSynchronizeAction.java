package org.eclipse.team.svn.ui.internal.subscriber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.subversion.client.ISVNStatus;
import org.eclipse.subversion.client.SVNStatusUtils;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.commands.GetStatusCommand;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

public class OverrideAndUpdateSynchronizeAction extends SynchronizeModelAction {

	public OverrideAndUpdateSynchronizeAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				SyncInfoDirectionFilter filter = new SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING,SyncInfo.CONFLICTING});
				if (!filter.select(info)) return false;
			    IStructuredSelection selection = getStructuredSelection();
			    boolean removeUnAdded  = SVNUIPlugin.getPlugin().getPreferenceStore().getBoolean(ISVNUIConstants.PREF_REMOVE_UNADDED_RESOURCES_ON_REPLACE);
			    Iterator iter = selection.iterator();
			    while (iter.hasNext()) {
			    	ISynchronizeModelElement element = (ISynchronizeModelElement)iter.next();
			    	IResource resource = element.getResource();
			    	if (resource == null || !resource.exists()) return false;
			    	if(!removeUnAdded)
			    	{
			    		 ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);			    
	                try {
	                	if (!svnResource.isManaged()) return false;
	                } catch (SVNException e) {
	                    return false;
	                }			    		
			    	}
			    }
                return true;
			}
		};
	}

	protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IStructuredSelection selection = getStructuredSelection();
	    ArrayList selectedElements = new ArrayList();
	    Iterator iter = selection.iterator();
		while (iter.hasNext()) {
			ISynchronizeModelElement synchronizeModelElement = (ISynchronizeModelElement)iter.next();
			IResource resource = synchronizeModelElement.getResource();
			selectedElements.add(resource);
		}
		IResource[] resources = new IResource[selectedElements.size()];
		selectedElements.toArray(resources); 
		IResource[] modifiedResources = null;
		try {
			modifiedResources = getModifiedResources(resources, new NullProgressMonitor());					
		} catch (SVNException e) {
            
        }
		return new OverrideAndUpdateSynchronizeOperation(configuration, elements, modifiedResources, resources);
	}
	
	private IResource[] getModifiedResources(IResource[] resources, IProgressMonitor iProgressMonitor) throws SVNException {
	    final List modified = new ArrayList();
	    for (int i = 0; i < resources.length; i++) {
			 IResource resource = resources[i];
			 ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
			 
			 // get adds, deletes, updates and property updates.
			 GetStatusCommand command = new GetStatusCommand(svnResource, true, false);
			 command.run(iProgressMonitor);
			 ISVNStatus[] statuses = command.getStatuses();
			 for (int j = 0; j < statuses.length; j++) {
			     if (SVNStatusUtils.isReadyForRevert(statuses[j])  ||
			   		  !SVNStatusUtils.isManaged(statuses[j])) {
			         IResource currentResource = SVNWorkspaceRoot.getResourceFor(statuses[j]);
			         if (currentResource != null)
			             modified.add(currentResource);
			     }
			 }
		}
	    return (IResource[]) modified.toArray(new IResource[modified.size()]);
	}

}
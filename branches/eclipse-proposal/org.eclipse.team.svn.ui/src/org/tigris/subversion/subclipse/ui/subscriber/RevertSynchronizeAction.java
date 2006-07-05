package org.tigris.subversion.subclipse.ui.subscriber;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.synchronize.ChangeSetDiffNode;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.commands.GetStatusCommand;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.core.internal.util.Util;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

public class RevertSynchronizeAction extends SynchronizeModelAction {
	private String url;

    public RevertSynchronizeAction(String text, ISynchronizePageConfiguration configuration) {
        super(text, configuration);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSyncInfoFilter()
	 */
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				SyncInfoDirectionFilter outgoingFilter = new SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING, SyncInfo.CONFLICTING});
			    if (!outgoingFilter.select(info)) return false;
			    IStructuredSelection selection = getStructuredSelection();
			    Iterator iter = selection.iterator();
			    boolean removeUnAdded  = SVNUIPlugin.getPlugin().getPreferenceStore().getBoolean(ISVNUIConstants.PREF_REMOVE_UNADDED_RESOURCES_ON_REPLACE);
			    
			    while (iter.hasNext()) {
			    	ISynchronizeModelElement element = (ISynchronizeModelElement)iter.next();
			    	IResource resource = element.getResource();
			    	if (resource == null) continue;
			    	if (resource.isLinked()) return false;
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
		url = null;
	    IStructuredSelection selection = getStructuredSelection();
	    if (selection.size() == 1) {
	        ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
		    IResource resource = element.getResource();
		    if (resource != null) {
			    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
	            try {
	                url = svnResource.getStatus().getUrlString();
	                if ((url == null) || (resource.getType() == IResource.FILE)) url = Util.getParentUrl(svnResource);
	            } catch (SVNException e) {
	                e.printStackTrace();
	            }
		    }
	    }
	    List selectedElements = new ArrayList();
	    Iterator iter = selection.iterator();
		while (iter.hasNext()) {
			ISynchronizeModelElement synchronizeModelElement = (ISynchronizeModelElement)iter.next();
			if (synchronizeModelElement instanceof ChangeSetDiffNode) {
				// If we find a ChangeSet we ignore the rest, even following Change Sets.
				selectedElements.clear();
				ChangeSet set = ((ChangeSetDiffNode)synchronizeModelElement).getSet();
				selectedElements = Arrays.asList(set.getResources());
				break;
			} else {
				IResource resource = synchronizeModelElement.getResource();
				selectedElements.add(resource);
			}
		}
		IResource[] resources = new IResource[selectedElements.size()];
		selectedElements.toArray(resources); 
		IResource[] modifiedResources = null;
		try {
			modifiedResources = getModifiedResources(resources, new NullProgressMonitor());					
		} catch (SVNException e) {
            
        }
		return new RevertSynchronizeOperation(configuration, elements, url, modifiedResources);
    }
    
	private IResource[] getModifiedResources(IResource[] resources, IProgressMonitor iProgressMonitor) throws SVNException {
	    final List modified = new ArrayList();
	    for (int i = 0; i < resources.length; i++) {
			 IResource resource = resources[i];
			 ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
			 
			 // if only one resource selected, get url.  Revert dialog displays this.
			 if (resources.length == 1) {
				   url = svnResource.getStatus().getUrlString();
				   if ((url == null) || (resource.getType() == IResource.FILE)) url = Util.getParentUrl(svnResource);
			 }
			 
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

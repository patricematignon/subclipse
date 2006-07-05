package org.eclipse.team.svn.ui.internal.subscriber;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

public class ShowPropertiesSynchronizeAction extends SynchronizeModelAction {

	public ShowPropertiesSynchronizeAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
				SyncInfoDirectionFilter outgoingFilter = new SyncInfoDirectionFilter(new int[] {SyncInfo.OUTGOING});
			    if (!outgoingFilter.select(info)) return false;
				IStructuredSelection selection = getStructuredSelection();
			    if (selection.size() != 1) return false;
		        ISynchronizeModelElement element = (ISynchronizeModelElement)selection.getFirstElement();
			    IResource resource = element.getResource();
		        if (resource == null) return false;
                ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);			    
                try {
                    return !svnResource.getStatus().isDeleted() && svnResource.getStatus().isManaged() && resource.exists();
                } catch (SVNException e) {
                    return false;
                }
			}
		};
	}    

	protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
        ISynchronizeModelElement element = (ISynchronizeModelElement)getStructuredSelection().getFirstElement();
        IResource resource = element.getResource();		
		return new ShowPropertiesSynchronizeOperation(configuration, elements, resource);
	}

}

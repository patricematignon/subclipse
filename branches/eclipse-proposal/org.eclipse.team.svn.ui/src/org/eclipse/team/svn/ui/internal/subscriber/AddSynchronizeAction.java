package org.eclipse.team.svn.ui.internal.subscriber;

import java.util.Iterator;

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

public class AddSynchronizeAction extends SynchronizeModelAction {

	public AddSynchronizeAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter() {
			public boolean select(SyncInfo info) {
			    IStructuredSelection selection = getStructuredSelection();
			    Iterator iter = selection.iterator();
			    while (iter.hasNext()) {
			    	ISynchronizeModelElement element = (ISynchronizeModelElement)iter.next();
			    	IResource resource = element.getResource();
			    	if (resource == null) return false;
			    	if (resource.isLinked()) return false;
	                ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);			    
	                try {
	                	if (svnResource.isManaged()) return false;
	                } catch (SVNException e) {
	                    return false;
	                }
			    }
                return true;
			}
		};
	}    

	protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		return new AddSynchronizeOperation(configuration, elements);
	}

}
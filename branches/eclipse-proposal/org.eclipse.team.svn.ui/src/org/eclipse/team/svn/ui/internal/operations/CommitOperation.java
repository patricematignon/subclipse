package org.eclipse.team.svn.ui.internal.operations;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNProviderPlugin;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.IWorkbenchPart;

public class CommitOperation extends SVNOperation {
//    private IResource[] selectedResources;
    private IResource[] resourcesToAdd;
    private IResource[] resourcesToDelete;
    private IResource[] resourcesToCommit;
    private String commitComment;
    private boolean keepLocks;
    private ISVNClientAdapter svnClient;

    public CommitOperation(IWorkbenchPart part, IResource[] selectedResources, IResource[] resourcesToAdd, IResource[] resourcesToDelete, IResource[] resourcesToCommit, String commitComment, boolean keepLocks) {
        super(part);
//        this.selectedResources = selectedResources;
        this.resourcesToAdd = resourcesToAdd;
        this.resourcesToDelete = resourcesToDelete;
        this.resourcesToCommit = resourcesToCommit;
        this.commitComment = commitComment;
        this.keepLocks = keepLocks;
    }

    protected void execute(IProgressMonitor monitor) throws SVNException, InterruptedException {
        try {
        	svnClient = SVNProviderPlugin.getPlugin().getSVNClientManager().createSVNClient();
        	if (resourcesToAdd.length > 0) {
			    Map table = getProviderMapping(resourcesToAdd);
				if (table.get(null) != null) {
					throw new SVNException(Policy.bind("RepositoryManager.addErrorNotAssociated"));  //$NON-NLS-1$
				}
				Set keySet = table.keySet();
				Iterator iterator = keySet.iterator();
				while (iterator.hasNext()) {
					SVNTeamProvider provider = (SVNTeamProvider)iterator.next();
					List list = (List)table.get(provider);
					IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
					provider.add(providerResources, IResource.DEPTH_ZERO, null);
				}						
			}
        	if (resourcesToDelete.length > 0) {
				ISVNClientAdapter svnDeleteClient = null; // use an adapter that will log to console
			    Map table = getProviderMapping(resourcesToDelete);
				if (table.get(null) != null) {
					throw new SVNException(Policy.bind("RepositoryManager.addErrorNotAssociated"));  //$NON-NLS-1$
				}
				Set keySet = table.keySet();
				Iterator iterator = keySet.iterator();
				while (iterator.hasNext()) {
					SVNTeamProvider provider = (SVNTeamProvider)iterator.next();
					List list = (List)table.get(provider);
					IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
					File[] files = new File[providerResources.length];
					for (int i = 0; i < providerResources.length; i++) {
						ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(providerResources[i]);
						if (svnDeleteClient == null)
						    svnDeleteClient = svnResource.getRepository().getSVNClient();
						files[i] = svnResource.getFile();
					}
					try {
						svnDeleteClient.remove(files, true);
					} catch (SVNClientException e) {
						throw new TeamException(e.getMessage());
					}
				}						
			}
			Map table = getProviderMapping(resourcesToCommit);
			Set keySet = table.keySet();
			Iterator iterator = keySet.iterator();
	        monitor.beginTask(null, 100 * keySet.size());
			while (iterator.hasNext()) {
				SVNTeamProvider provider = (SVNTeamProvider)iterator.next();
				List list = (List)table.get(provider);
				IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
				provider.checkin(providerResources, commitComment, keepLocks, getDepth(providerResources), Policy.subMonitorFor(monitor, 100));
			}			
//			for (int i = 0; i < selectedResources.length; i++) {
//				IResource projectHandle = selectedResources[i].getProject();
//				projectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
//			}		
        } catch (TeamException e) {
			throw SVNException.wrapException(e);
//		} catch (CoreException e) {
//			throw SVNException.wrapException(e);
		} finally {
			monitor.done();
		}
    }

    /**
	 * This method figures out of if we should commit with DEPTH_ZERO or DEPTH_INFINITE
	 * If there are any modified folders (which could only be a prop change) in the list of committed items,
	 * then it should return DEPTH_ZERO, otherwise it should return DEPTH_INFINITE.
	 * @param resources an array of resources to check
	 * @return IResource.DEPTH_ZERO or IResource.DEPTH_INFINITE  
	 */
	private int getDepth(IResource[] resources) {
	    int depth = IResource.DEPTH_INFINITE;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType() != IResource.FILE) {
				ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resources[i]);
				try {
				    // If there is a folder delete, then we cannot do a
				    // non-recursive commit
					if (svnResource.getStatus().isDeleted())
						return IResource.DEPTH_INFINITE;
					if (svnResource.getStatus().isPropModified())
						depth = IResource.DEPTH_ZERO;
				} catch (SVNException e) {
				}
			}
		}
		return depth;
	}

	protected String getTaskName() {
        return Policy.bind("CommitOperation.taskName"); //$NON-NLS-1$;
    }
    
	private Map getProviderMapping(IResource[] resources) {
		RepositoryProvider provider = null;
		Map result = new HashMap();
		for (int i = 0; i < resources.length; i++) {
			if (provider == null || !svnClient.canCommitAcrossWC())
				provider = RepositoryProvider.getProvider(resources[i].getProject(), SVNProviderPlugin.getTypeId());
			List list = (List)result.get(provider);
			if (list == null) {
				list = new ArrayList();
				result.put(provider, list);
			}
			list.add(resources[i]);
		}
		return result;
	}

}
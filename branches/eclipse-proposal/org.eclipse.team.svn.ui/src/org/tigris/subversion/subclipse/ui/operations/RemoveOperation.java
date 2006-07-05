package org.tigris.subversion.subclipse.ui.operations;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.ui.Policy;

public class RemoveOperation extends SVNOperation {
	private IResource[] resources;

	public RemoveOperation(IWorkbenchPart part, IResource[] resources) {
		super(part);
		this.resources = resources;
	}

	protected void execute(IProgressMonitor monitor) throws SVNException, InterruptedException {
		ISVNClientAdapter client = null; 
		ArrayList files = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resources[i]);
			if (client == null)
			    client = svnResource.getRepository().getSVNClient();			
			files.add(svnResource.getFile());
		}
		File[] fileArray = new File[files.size()];
		files.toArray(fileArray);
		try {
			client.remove(fileArray, true);
			refresh();
		} catch (SVNClientException e) {
			throw SVNException.wrapException(e);
		}
		finally {
			monitor.done();
		}
	}

	protected String getTaskName() {
		return Policy.bind("MarkDeletedAction.label"); //$NON-NLS-1$;
	}
	
	private void refresh() {
		ArrayList parents = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			IResource parent = null;
			while (resource != null) {
				resource = resource.getParent();
				if (resource != null) parent = resource;
			}
			if (parent != null && !parents.contains(parent)) parents.add(parent);
		}
		Iterator iter = parents.iterator();
		while (iter.hasNext()) {
			IResource parent = (IResource)iter.next();
			try {
				parent.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (CoreException e) {}
		}
	}

}

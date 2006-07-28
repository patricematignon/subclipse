/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal.operations;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.SVNTeamProvider;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Panagiotis K
 */
public class ReplaceOperation extends UpdateOperation {
	
    private final SVNRevision revision;


	/**
     * @param part
     * @param resources
     */
    public ReplaceOperation(IWorkbenchPart part, IResource[] resources, SVNRevision revision, boolean recursive) {
        super(part, resources, revision, recursive);
		this.revision = revision;
    }

    /**
     * @param part
     * @param resource
     */
    public ReplaceOperation(IWorkbenchPart part, IResource resource, SVNRevision revision) {
        super(part, resource, revision);
        this.revision = revision;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
	 */
	protected String getTaskName() {
		return Policy.bind("ReplaceOperation.taskName"); //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation#getTaskName(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected String getTaskName(SVNTeamProvider provider) {
		return Policy.bind("ReplaceOperation.0", provider.getProject().getName()); //$NON-NLS-1$
	}


    /* (non-Javadoc)
     * @see org.eclipse.team.svn.ui.internal.operations.RepositoryProviderOperation#execute(org.eclipse.team.svn.core.internal.SVNTeamProvider, org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void execute(SVNTeamProvider provider, IResource[] resources, IProgressMonitor monitor) throws SVNException, InterruptedException {
        monitor.beginTask(null, 100);
		try {
			boolean removeUnAdded  = SVNUIPlugin.getPlugin().getPreferenceStore().getBoolean(ISVNUIConstants.PREF_REMOVE_UNADDED_RESOURCES_ON_REPLACE);
            // first we revert to base (otherwise it will do a merge instead of
            // replace resources)
		    for (int i = 0; i < resources.length; i++) {
                IResource resource = resources[i];

                ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                if(!localResource.isManaged() && removeUnAdded)
        			{
        				try
    					{
    						resource.delete(true, monitor);
    					}
    					catch (CoreException ex)
    					{
    						throw SVNException.wrapException(ex);
    					}
        			}
                else if (localResource.isDirty()) {
               	 localResource.revert();
                }
            }

		    // We are already at the base revision after a revert, no need to update
		    if (this.revision.equals(SVNRevision.BASE)) {
		    	return;
		    }
		    
            // then we update to revision
		    super.execute(provider, resources, monitor);
		} catch (SVNException e) {
		    collectStatus(e.getStatus());
		} finally {
			monitor.done();
		}
    }
}

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
package org.eclipse.team.svn.core.internal.resources;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNAnnotations;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.Policy;

/**
 * Represents the base revision of a file 
 * 
 */
public class BaseFile extends BaseResource implements ISVNRemoteFile {
	
	public BaseFile(LocalResourceStatus localResourceStatus)
	{
		super(localResourceStatus);
	}	

	public BaseFile(LocalResourceStatus localResourceStatus, String charset) {
		super(localResourceStatus, charset);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#isContainer()
	 */
	public boolean isContainer() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariant#getStorage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStorage getStorage(IProgressMonitor monitor) throws TeamException
	{
		return new BaseResourceStorage(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNRemoteResource#members(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISVNRemoteResource[] members(IProgressMonitor progress){
		return new ISVNRemoteResource[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.svn.core.internal.ISVNRemoteFile#getAnnotations(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISVNAnnotations getAnnotations(IProgressMonitor monitor) throws TeamException {
		monitor = Policy.monitorFor(monitor);
		monitor.beginTask(Policy.bind("RemoteFile.getAnnotations"), 100);//$NON-NLS-1$
		try {
			ISVNClientAdapter svnClient = getRepository().getSVNClient();
			try {
				return svnClient.annotate(localResourceStatus.getFile(), null,
						getRevision());
			} catch (SVNClientException e) {
				throw new TeamException(
						"Failed in BaseFile.getAnnotations()", e);
			}
		} finally {
			monitor.done();
		}	
	}		
}

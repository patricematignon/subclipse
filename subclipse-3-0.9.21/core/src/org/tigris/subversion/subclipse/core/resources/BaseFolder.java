/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.Policy;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNStatus;
import org.tigris.subversion.subclipse.core.util.Assert;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * represents the base revision of a folder
 * 
 */
public class BaseFolder extends RemoteFolder implements ISVNRemoteFolder {
	private ISVNLocalResource localResource;
	
	/**
	 * Constructor for RemoteFolder.
	 * @throws SVNException
	 */
	public BaseFolder( 
			ISVNLocalResource localResource,
	        SVNRevision.Number lastChangedRevision,
	        Date date,
	        String author) throws SVNException {
		super(null, localResource.getRepository(), localResource.getUrl(), SVNRevision.BASE, lastChangedRevision, date, author);
		Assert.isNotNull(localResource);
		this.localResource = localResource;
	}
	
	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.core.resources.RemoteFolder#getMembers(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected ISVNRemoteResource[] getMembers(IProgressMonitor monitor)
			throws SVNException {
		// we can't use the svnurl to get the members of a base folder, that's
		// why we needed to override this method
		
		final IProgressMonitor progress = Policy.monitorFor(monitor);
		progress.beginTask(Policy.bind("RemoteFolder.getMembers"), 100); //$NON-NLS-1$
        
        if (children != null)
        {
            progress.done();
            return children;
        }
		
		try {
            ISVNClientAdapter client = getRepository().getSVNClient();
				
			ISVNDirEntry[] list = client.getList(localResource.getFile(),getRevision(),false);
			List result = new ArrayList();

			IContainer container = (IContainer)localResource.getIResource();			
			// directories first				
			for (int i=0;i<list.length;i++)
			{
                ISVNDirEntry entry = list[i];
                if (entry.getNodeKind() == SVNNodeKind.DIR)
				{
				    result.add(new BaseFolder(
	                   new LocalFolder(container.getFolder(new Path(entry.getPath()))), 
                       entry.getLastChangedRevision(),
                       entry.getLastChangedDate(),
                       entry.getLastCommitAuthor()));
				}
			}

			// files then				
			for (int i=0;i<list.length;i++)
			{
				ISVNDirEntry entry = list[i];
				if (entry.getNodeKind() == SVNNodeKind.FILE)
				{
					result.add(new BaseFile(
                        new LocalFile(container.getFile(new Path(entry.getPath()))), 
                        entry.getLastChangedRevision(),
                        entry.getLastChangedDate(),
                        entry.getLastCommitAuthor()));
			     }
					 	
			}

			children = (ISVNRemoteResource[])result.toArray(new ISVNRemoteResource[0]);
            return children;
        } catch (SVNClientException e)
		{
            throw new SVNException(new SVNStatus(SVNStatus.ERROR, SVNStatus.DOES_NOT_EXIST, Policy.bind("RemoteFolder.doesNotExist", getRepositoryRelativePath()))); //$NON-NLS-1$
        } finally {
			progress.done();
		}	 	
	}
}

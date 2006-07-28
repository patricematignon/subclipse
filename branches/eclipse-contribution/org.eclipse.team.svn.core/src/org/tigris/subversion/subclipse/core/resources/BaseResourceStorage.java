/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.subclipse.core.resources;

import java.io.InputStream;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;

/**
 * IStorage implementation for accessing the contents of base resource
 *
 */
public class BaseResourceStorage extends PlatformObject implements IStorage ,IEncodedStreamContentAccessor, IEncodedStorage {

	private BaseResource baseResource;
	
	public BaseResourceStorage(BaseResource baseResource)
	{
		super();
		this.baseResource = baseResource;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getContents()
	 */
	public InputStream getContents() throws CoreException {
		ISVNClientAdapter svnClient = baseResource.getRepository().getSVNClient();
		try {
			return svnClient.getContent(baseResource.getFile(), baseResource.getRevision());
		} catch (SVNClientException e) {
			throw new CoreException(new Status(IStatus.ERROR, SVNProviderPlugin.ID, 0, "Failed in BaseFile.getContents()", e)); //$NON-NLS-1$);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getFullPath()
	 */
	public IPath getFullPath() {
		return baseResource.getPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getName()
	 */
	public String getName() {
		return baseResource.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	public String getCharset() throws CoreException {
		return baseResource.getCharset();
	}
}

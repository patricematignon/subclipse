/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.annotations;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.core.TeamPlugin;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

public class RemoteAnnotationStorage extends PlatformObject implements IEncodedStorage  {

	private InputStream contents;
	private ISVNRemoteFile file;
	
	public RemoteAnnotationStorage(ISVNRemoteFile file, InputStream contents) {
		this.file = file;
		this.contents = contents;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getContents()
	 */
	public InputStream getContents() throws CoreException {
		try {
			// Contents are a ByteArrayInputStream which can be reset to the beginning
			contents.reset();
		} catch (IOException e) {
			SVNUIPlugin.log(SVNException.wrapException(e));
		}
		return contents;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IEncodedStorage#getCharset()
	 */
	public String getCharset() throws CoreException {
		InputStream contents = getContents();
		try {
			String charSet = TeamPlugin.getCharset(getName(), contents);
			return charSet;
		} catch (IOException e) {
			throw new SVNException(new Status(IStatus.ERROR, SVNUIPlugin.ID, IResourceStatus.FAILED_DESCRIBING_CONTENTS, Policy.bind("RemoteAnnotationStorage.1", getFullPath().toString()), e)); //$NON-NLS-1$
		} finally {
			try {
				contents.close();
			} catch (IOException e1) {
				// Ignore
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getFullPath()
	 */
	public IPath getFullPath() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getName()
	 */
	public String getName() {
		return file.getName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}
}

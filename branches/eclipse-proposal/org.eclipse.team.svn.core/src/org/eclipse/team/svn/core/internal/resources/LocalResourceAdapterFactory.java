/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     C�dric Chabanois (cchabanois@ifrance.com) 
 *******************************************************************************/
package org.eclipse.team.svn.core.internal.resources;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.svn.core.internal.ISVNLocalFile;
import org.eclipse.team.svn.core.internal.ISVNLocalFolder;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;

/**
 * AdaptorFactory used to translate IResource in ISVNLocalResource if possible
 */
public class LocalResourceAdapterFactory implements IAdapterFactory {

	private static Class[] SUPPORTED_TYPES = new Class[] { ISVNLocalResource.class, ISVNLocalFile.class, ISVNLocalFolder.class};

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!(adaptableObject instanceof IResource)) {
			adaptableObject = ((IAdaptable)adaptableObject).getAdapter(IResource.class);
		}
		
		if (adaptableObject instanceof IResource) {
			IResource resource = (IResource)adaptableObject; 
			if (ISVNLocalResource.class.equals(adapterType)) {
				return SVNWorkspaceRoot.getSVNResourceFor(resource);							
			}
			if ((ISVNLocalFile.class.equals(adapterType)) && (adaptableObject instanceof IFile)) {
				IFile file = (IFile)resource;
				return SVNWorkspaceRoot.getSVNFileFor(file);
			}
			if ((ISVNLocalFolder.class.equals(adapterType)) && (adaptableObject instanceof IContainer)) {
				IContainer container = (IContainer)resource;
				return SVNWorkspaceRoot.getSVNFolderFor(container);
			}			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return SUPPORTED_TYPES;
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion
 *******************************************************************************/
package org.tigris.subversion.subclipse.ui.repository.model;


import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFile;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.ui.repository.properties.SVNRemoteResourcePropertySource;

public class SVNAdapterFactory implements IAdapterFactory {
	private Object fileAdapter = new RemoteFileElement();
	private Object folderAdapter = new RemoteFolderElement();
	private Object rootAdapter = new SVNRepositoryRootElement();

	// Property cache
	private Object cachedPropertyObject = null;
	private Object cachedPropertyValue = null;

	/**
	 * Method declared on IAdapterFactory.
     * Get the given adapter for the given object
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IWorkbenchAdapter.class == adapterType) {
			return getWorkbenchAdapter(adaptableObject);
		}

		if(IDeferredWorkbenchAdapter.class == adapterType) {
			 Object o = getWorkbenchAdapter(adaptableObject);
			 if(o != null && o instanceof IDeferredWorkbenchAdapter) {
			 	return o;
			 }
			 return null;
		}

		if (IPropertySource.class == adapterType) {
			return getPropertySource(adaptableObject);
		}
		return null;
	}

	private Object getWorkbenchAdapter(Object adaptableObject) {
		if (adaptableObject instanceof ISVNRemoteFile) {
			return fileAdapter;
		} else if (adaptableObject instanceof ISVNRepositoryLocation) {
			return rootAdapter;
		} else if (adaptableObject instanceof ISVNRemoteFolder) {
			return folderAdapter;
		}
		return null;
	}
	/** (Non-javadoc)
	 * Method declared on IAdapterFactory.
	 */
	public Class[] getAdapterList() {
		return new Class[] {IWorkbenchAdapter.class, IPropertySource.class, IDeferredWorkbenchAdapter.class};
	}
	/**
	 * Returns the property source for the given object.  Caches
	 * the result because the property sheet is extremely inefficient,
	 * it asks for the source seven times in a row.
	 */
	public Object getPropertySource(Object adaptableObject) {
		if (adaptableObject == cachedPropertyObject) {
			return cachedPropertyValue;
		}
		cachedPropertyObject = adaptableObject;
		if (adaptableObject instanceof ISVNRemoteResource) {
			cachedPropertyValue = new SVNRemoteResourcePropertySource((ISVNRemoteResource)adaptableObject);
//		} else if (adaptableObject instanceof ISVNRepositoryLocation) {
//			cachedPropertyValue = new SVNRepositoryLocationPropertySource((ISVNRepositoryLocation)adaptableObject);
//		}  else if (adaptableObject instanceof RepositoryRoot) {
//			cachedPropertyValue = new SVNRepositoryLocationPropertySource(((RepositoryRoot)adaptableObject).getRoot());
		} else {
			cachedPropertyValue = null;
		}
		return cachedPropertyValue;
	}
}

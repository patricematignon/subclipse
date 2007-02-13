/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal.repository.model;


import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;
import org.eclipse.team.svn.ui.internal.history.SVNHistoryPageSource;
import org.eclipse.team.svn.ui.internal.repository.properties.SVNRemoteResourcePropertySource;
import org.eclipse.team.ui.history.IHistoryPageSource;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

public class SVNAdapterFactory implements IAdapterFactory {
	private Object fileAdapter = new RemoteFileElement();
	private Object folderAdapter = new RemoteFolderElement();
	private Object rootAdapter = new SVNRepositoryRootElement();
	private Object pageHistoryParticipant = new SVNHistoryPageSource();

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
        
        if(IHistoryPageSource.class == adapterType) {
          return pageHistoryParticipant;
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
		return new Class[] {IWorkbenchAdapter.class, IPropertySource.class, IDeferredWorkbenchAdapter.class, IHistoryPageSource.class};
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

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

 
import java.util.Arrays;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;
import org.eclipse.team.svn.core.internal.repo.RepositoryComparator;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * AllRootsElement is the model element for the repositories view.
 * Its children are the array of all known repository roots.
 * 
 * Because we extend IAdaptable, we don't need to register this adapter
 * as we need for RemoteFileElement, RemoteFolderElement ...
 */
public class AllRootsElement extends SVNModelElement  implements IAdaptable  {
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}
	public Object[] internalGetChildren(Object o, IProgressMonitor monitor) {
        ISVNRepositoryLocation[] locations =
            SVNUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations(null);
        Arrays.sort(locations, new RepositoryComparator());
        return locations; 	
    }
	public String getLabel(Object o) {
		return null;
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	public Object getParent(Object o) {
		return null;
	}
}


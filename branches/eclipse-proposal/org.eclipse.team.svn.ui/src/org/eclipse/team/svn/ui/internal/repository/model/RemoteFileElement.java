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
package org.eclipse.team.svn.ui.internal.repository.model;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.ui.PlatformUI;

public class RemoteFileElement extends SVNModelElement {
	/**
	 * Initial implementation: return null;
	 */
	public Object[] internalGetChildren(Object o, IProgressMonitor monitor) {
		return new Object[0];
	}
	/**
	 * Initial implementation: return null.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (!(object instanceof ISVNRemoteFile)) return null;
		return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(((ISVNRemoteFile)object).getName());
	}
	/**
	 * Initial implementation: return the file's name and version
	 */
	public String getLabel(Object o) {
		if (!(o instanceof ISVNRemoteFile)) return null;
		ISVNRemoteFile file = (ISVNRemoteFile)o;
		return Policy.bind("nameAndRevision", file.getName(),file.getLastChangedRevision().toString()); //$NON-NLS-1$
	}
    
    /**
     * Return null.
     */
    public Object getParent(Object o) {
        return null;
    }    
    
}

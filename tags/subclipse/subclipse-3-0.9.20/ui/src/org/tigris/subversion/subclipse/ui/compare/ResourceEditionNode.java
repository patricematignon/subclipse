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
package org.tigris.subversion.subclipse.ui.compare;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
/**
 * A class for comparing ISVNRemoteResource objects
 * 
 * <p>
 * <pre>
 * ResourceEditionNode left = new ResourceEditionNode(editions[0]);
 * ResourceEditionNode right = new ResourceEditionNode(editions[1]);
 * CompareUI.openCompareEditorOnPage(new SVNCompareEditorInput(left, right), getTargetPage());
 * </pre>
 * </p>
 *  
 */
public class ResourceEditionNode
		implements
			IStructureComparator,
			ITypedElement,
			IStreamContentAccessor {
	private ISVNRemoteResource resource;
	private ResourceEditionNode[] children;
	/**
	 * Creates a new ResourceEditionNode on the given resource edition.
	 */
	public ResourceEditionNode(ISVNRemoteResource resourceEdition) {
		this.resource = resourceEdition;
	}
	/*
	 * get the remote resource for this node
	 */
	public ISVNRemoteResource getRemoteResource() {
		return resource;
	}
	/**
	 * Returns true if both resources names are identical. The content is not
	 * considered.
	 * 
	 * @see IComparator#equals
	 */
	public boolean equals(Object other) {
		if (other instanceof ITypedElement) {
			String otherName = ((ITypedElement) other).getName();
			return getName().equals(otherName);
		}
		return super.equals(other);
	}
	/**
	 * Enumerate children of this node (if any).
	 * 
	 * @see IStructureComparator#getChildren
	 */
	public Object[] getChildren() {
		if (children == null) {
			children = new ResourceEditionNode[0];
			if (resource != null) {
				try {
					SVNUIPlugin.runWithProgress(null, true /* cancelable */,
							new IRunnableWithProgress() {
								public void run(IProgressMonitor monitor)
										throws InvocationTargetException {
									try {
										ISVNRemoteResource[] members = resource
												.members(monitor);
										children = new ResourceEditionNode[members.length];
										for (int i = 0; i < members.length; i++) {
											children[i] = new ResourceEditionNode(
													members[i]);
										}
									} catch (TeamException e) {
										throw new InvocationTargetException(e);
									}
								}
							});
				} catch (InterruptedException e) {
					// operation canceled
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if (t instanceof TeamException) {
						SVNUIPlugin.log(((TeamException) t).getStatus());
					}
				}
			}
		}
		return children;
	}
	/**
	 * @see IStreamContentAccessor#getContents()
	 */
	public InputStream getContents() throws CoreException {
		if (resource == null || resource.isContainer()) {
			return null;
		}
		try {
			final InputStream[] holder = new InputStream[1];
			SVNUIPlugin.runWithProgress(null, true,
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) {
							try {
								holder[0] = resource.getStorage(monitor).getContents();
							} catch (CoreException e1) {
								SVNUIPlugin.log(e1);
							}
						}
					});
			return holder[0];
		} catch (InterruptedException e) {
			// operation canceled
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof TeamException) {
				throw new CoreException(((TeamException) t).getStatus());
			}
			// should not get here
		}
		return new ByteArrayInputStream(new byte[0]);
	}
	/*
	 * @see org.eclipse.compare.ITypedElement#getImage()
	 */
	public Image getImage() {
		return CompareUI.getImage(resource);
	}
	/*
	 * Returns the name of this node.
	 * 
	 * @see org.eclipse.compare.ITypedElement#getName()
	 */
	public String getName() {
		return resource == null ? "" : resource.getName(); //$NON-NLS-1$
	}
	/**
	 * Returns the comparison type for this node.
	 * 
	 * @see org.eclipse.compare.ITypedElement#getType()
	 */
	public String getType() {
		if (resource == null) {
			return UNKNOWN_TYPE;
		}
		if (resource.isContainer()) {
			return FOLDER_TYPE;
		}
		String name = resource.getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		return name.length() == 0 ? UNKNOWN_TYPE : name;
	}
	/**
	 * @see IComparator#equals
	 */
	public int hashCode() {
		return getName().hashCode();
	}
}
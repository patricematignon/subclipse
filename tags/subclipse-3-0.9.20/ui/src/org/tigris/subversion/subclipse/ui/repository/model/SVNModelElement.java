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


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

/**
 * Base class for model elements 
 */
public abstract class SVNModelElement implements IWorkbenchAdapter {
	
	private IRunnableContext runnableContext;
	
	/**
	 * Handles exceptions that occur in SVN model elements.
	 */
	protected void handle(Throwable t) {
		SVNUIPlugin.openError(null, null, null, t, SVNUIPlugin.LOG_NONTEAM_EXCEPTIONS);
	}
	
	/**
	 * Gets the children of the receiver by invoking the <code>internalGetChildren</code>.
	 * A appropriate progress indicator will be used if requested.
	 */
	public Object[] getChildren(final Object o, boolean needsProgress) {
		try {
			if (needsProgress) {
				final Object[][] result = new Object[1][];
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException {
						try {
							result[0] = SVNModelElement.this.internalGetChildren(o, monitor);
						} catch (TeamException e) {
							throw new InvocationTargetException(e);
						}
					}
				};
				getRunnableContext().run(isInterruptable() /*fork*/, isInterruptable() /*cancelable*/, runnable);
				return result[0];
			} else {
				return internalGetChildren(o, null);
			}
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (TeamException e) {
			handle(e);
		}
		return new Object[0];
	}
	
	/**
	 * Method internalGetChildren.
	 * @param o
	 * @return Object[]
	 */
	public abstract Object[] internalGetChildren(Object o, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		return getChildren(o, isNeedsProgress());
	}

	public boolean isNeedsProgress() {
		return false;
	}
	
	public boolean isInterruptable() {
		return false;
	}
	
	/**
	 * Returns the runnableContext.
	 * @return IRunnableContext
	 */
	public IRunnableContext getRunnableContext() {
		if (runnableContext == null) {
			return new IRunnableContext() {
				public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
					SVNUIPlugin.runWithProgress(null, cancelable, runnable);
				}
			};
		}
		return runnableContext;
	}

}


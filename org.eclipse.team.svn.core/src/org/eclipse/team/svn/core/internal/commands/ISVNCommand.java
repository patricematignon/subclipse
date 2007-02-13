/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.core.internal.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.internal.SVNException;

/**
 * 
 */
public interface ISVNCommand {
	/**
	 * execute the command
	 * @param monitor
	 * @throws SVNException
	 */
	public abstract void run(IProgressMonitor monitor) throws SVNException;
}
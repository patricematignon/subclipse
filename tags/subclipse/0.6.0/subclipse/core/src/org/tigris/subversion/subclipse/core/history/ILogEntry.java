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
package org.tigris.subversion.subclipse.core.history;


import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;

/**
 * Instances of ILogEntry represent an entry for a SVN file that results
 * from the svn log command.
 * 
 */
public interface ILogEntry extends IAdaptable {

	/**
	 * Get the revision for the entry
	 */
	public long getRevision();
	
	/**
	 * Get the author of the revision
	 */
	public String getAuthor();
	
	/**
	 * Get the date the revision was committed
	 */
	public Date getDate();
	
	/**
	 * Get the comment for the revision
	 */
	public String getComment();
	
	/**
	 * Get the remote file for this entry
	 */
	public ISVNRemoteResource getRemoteResource();
	
}


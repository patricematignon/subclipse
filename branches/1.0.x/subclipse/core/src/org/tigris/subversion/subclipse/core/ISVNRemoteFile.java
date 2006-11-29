/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.tigris.subversion.subclipse.core;

import org.eclipse.team.core.TeamException;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * This interface represents a file in a repository. Instances of this interface
 * can be used to fetch the contents of the remote file.
 * 
 */
public interface ISVNRemoteFile extends ISVNRemoteResource, ISVNFile {

	/**
	 * Get annotations for the remote file
	 * 
	 * @param fromRevision
	 * @param toRevision
	 * @return annotations of the receiver for the specified range
	 * @throws TeamException
	 */
	public ISVNAnnotations getAnnotations(SVNRevision fromRevision,
			SVNRevision toRevision) throws TeamException;

}

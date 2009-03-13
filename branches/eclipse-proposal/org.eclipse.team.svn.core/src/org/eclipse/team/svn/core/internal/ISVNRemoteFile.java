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
package org.eclipse.team.svn.core.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNAnnotations;
import org.eclipse.team.core.TeamException;

 

 /**
  * This interface represents a file in a repository.
  * Instances of this interface can be used to fetch the contents
  * of the remote file.
  * 
  */
public interface ISVNRemoteFile extends ISVNRemoteResource, ISVNFile {

	/**
	 * Get annotations for the remote file
	 * @param monitor 
	 * @return
	 * @throws TeamException
	 */
	public ISVNAnnotations getAnnotations(IProgressMonitor monitor) throws TeamException;	
	
}

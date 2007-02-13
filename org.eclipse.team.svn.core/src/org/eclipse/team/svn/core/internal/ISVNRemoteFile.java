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


/*******************************************************************************
 * Copyright (c) 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.subversion.client.javahl;

import org.eclipse.subversion.client.SVNLogMessageChangePath;
import org.eclipse.subversion.client.SVNRevision;
import org.tigris.subversion.javahl.ChangePath;

/**
 * JavaHL specific implementation of the {@link ISVNLogMessageChangePath}. 
 * Actually just an adapter from {@link org.tigris.subversion.javahl.ChangePath}
 * 
 */
public class JhlLogMessageChangePath extends SVNLogMessageChangePath {
	
	/**
	 * Constructor
	 * @param changePath
	 */
	public JhlLogMessageChangePath(ChangePath changePath) {
		super(
				changePath.getPath(),
				(changePath.getCopySrcRevision() != -1) ? new SVNRevision.Number(
						changePath.getCopySrcRevision()) : null, 
				changePath.getCopySrcPath(), 
				changePath.getAction());
	}

}

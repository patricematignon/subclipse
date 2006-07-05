/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 
*******************************************************************************/
package org.eclipse.team.svn.ui.internal.actions;

import org.eclipse.subversion.client.SVNRevision;

/**
 */
public class ReplaceWithBaseRevisionAction extends ReplaceWithRemoteAction {

	public ReplaceWithBaseRevisionAction() {
		super(SVNRevision.BASE);
	}
}

/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.eclipse.team.svn.core.internal.sync;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.svn.core.internal.ISVNLocalResource;
import org.eclipse.team.svn.core.internal.ISVNRemoteResource;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.core.internal.resources.SVNWorkspaceRoot;
import org.eclipse.team.svn.core.internal.util.Util;

/**
 * @author mml
 */
public class SVNRevisionComparator implements IResourceVariantComparator {

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariantComparator#compare(org.eclipse.core.resources.IResource, org.eclipse.team.core.variants.IResourceVariant)
	 */
	public boolean compare(IResource local, IResourceVariant remote) {
		ISVNLocalResource a = SVNWorkspaceRoot.getSVNResourceFor(local);
		ISVNRemoteResource b = (ISVNRemoteResource)remote;
		if( local == null && remote == null )
		    return true;
		if( local == null || remote == null )
		    return false;
		try {
			return a.getStatus().getLastChangedRevision().getNumber() == b.getLastChangedRevision().getNumber();
		} catch (SVNException e) {
            Util.logError("Cannot compare local resource with remote resource",e);
		}
		return false;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariantComparator#compare(org.eclipse.team.core.variants.IResourceVariant, org.eclipse.team.core.variants.IResourceVariant)
	 */
	public boolean compare(IResourceVariant base, IResourceVariant remote) {
		ISVNRemoteResource a = (ISVNRemoteResource)base;
		ISVNRemoteResource b = (ISVNRemoteResource)remote;
		if( base == remote )
		    return true;
		if( base == null || remote == null )
		    return false;
		return a.getLastChangedRevision().getNumber()==b.getLastChangedRevision().getNumber();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariantComparator#isThreeWay()
	 */
	public boolean isThreeWay() {
		return true;
	}

}

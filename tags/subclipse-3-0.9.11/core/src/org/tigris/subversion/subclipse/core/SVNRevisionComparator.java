/*
 * Created on Jul 10, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigris.subversion.subclipse.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

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
		try {
			return a.getStatus().getLastChangedRevision().getNumber() == b.getLastChangedRevision().getNumber();
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariantComparator#compare(org.eclipse.team.core.variants.IResourceVariant, org.eclipse.team.core.variants.IResourceVariant)
	 */
	public boolean compare(IResourceVariant base, IResourceVariant remote) {
		ISVNRemoteResource a = (ISVNRemoteResource)base;
		ISVNRemoteResource b = (ISVNRemoteResource)remote;
		return a.getLastChangedRevision().getNumber()==a.getLastChangedRevision().getNumber();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariantComparator#isThreeWay()
	 */
	public boolean isThreeWay() {
		// TODO Auto-generated method stub
		return true;
	}

}

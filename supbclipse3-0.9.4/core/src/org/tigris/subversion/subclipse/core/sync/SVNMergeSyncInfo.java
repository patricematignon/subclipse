/*
 * Created on Apr 7, 2004
 */
package org.tigris.subversion.subclipse.core.sync;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

public class SVNMergeSyncInfo extends SVNSyncInfo{

	/**
	 * @param local
	 * @param base
	 * @param remote
	 * @param comparator
	 * @todo Generated comment
	 */
	public SVNMergeSyncInfo(IResource local, IResourceVariant base, IResourceVariant remote, IResourceVariantComparator comparator) {
		super(local, base, remote, comparator);
		
	}

	
}

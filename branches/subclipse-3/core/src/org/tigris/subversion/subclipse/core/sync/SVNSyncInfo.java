/*
 * Created on Apr 7, 2004
 *
 * @todo To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.tigris.subversion.subclipse.core.sync;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.tigris.subversion.subclipse.core.ISVNLocalFile;
import org.tigris.subversion.subclipse.core.ISVNLocalFolder;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.RemoteResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNStatus;

/**
 * @author mml
 * 
 * @todo To change the template for this generated type comment go to Window -
 *       Preferences - Java - Code Generation - Code and Comments
 */
public class SVNSyncInfo extends SyncInfo {

	/**
	 * @param local
	 * @param base
	 * @param remote
	 * @param comparator
	 * @todo Generated comment
	 */
	public SVNSyncInfo(IResource local, IResourceVariant base,
			IResourceVariant remote, IResourceVariantComparator comparator) {
		super(local, base, remote, comparator);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.synchronize.SyncInfo#calculateKind()
	 */
	protected int calculateKind() throws TeamException {

		RemoteResource remote = (RemoteResource) getRemote();
		ISVNLocalResource local = SVNWorkspaceRoot.getSVNResourceFor(getLocal());
		
		boolean remoteExists = (remote != null);
		
		
		if (!remoteExists && local.exists()) {
			return ADDITION;
		} else if (local.getStatus().getTextStatus() == ISVNStatus.Kind.MODIFIED) {
			return OUTGOING;
		} else if (!local.exists() && remoteExists) {
			return DELETION;
		} else if (!getComparator().compare(getLocal(), getRemote())) {
			return CHANGE;
		} else {
			return IN_SYNC;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.synchronize.SyncInfo#getLocalContentIdentifier()
	 */
	public String getLocalContentIdentifier() {
		IResource local = getLocal();
		try {
			if (local != null || local.getType() == IResource.FILE) {
				// it's a file, return the revision number if we can find one
				ISVNLocalFile cvsFile = SVNWorkspaceRoot
						.getSVNFileFor((IFile) local);
				return cvsFile.getStatus().getLastChangedRevision().toString();
			} else if (local != null || local.getType() == IResource.FOLDER) {
				ISVNLocalFolder svnFolder = SVNWorkspaceRoot
						.getSVNFolderFor((IFolder) local);
				return svnFolder.getStatus().getLastChangedRevision()
						.toString();
			}
		} catch (SVNException e) {
			SVNProviderPlugin.log(e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		return getLocal().getName() + " " + getLocalContentIdentifier();
	}

}
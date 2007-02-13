package org.eclipse.team.svn.ui.internal.repository;

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.ISVNRemoteFolder;
import org.eclipse.team.svn.core.internal.ISVNRepositoryLocation;

public class RepositorySorter extends ViewerSorter {

	private static final int REPO_ROOT_CATEGORY = 1;
	private static final int REMOTE_FOLDER_CATEGORY = 2;
	private static final int REMOTE_FILE_CATEGORY = 3;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	public int category(Object element) {
		if (element instanceof ISVNRepositoryLocation) {
			return REPO_ROOT_CATEGORY;
		}
		
		if (element instanceof ISVNRemoteFolder) {
			return REMOTE_FOLDER_CATEGORY;
		}
		
		if (element instanceof ISVNRemoteFile) {
			return REMOTE_FILE_CATEGORY;
		}
		
		return 0;
	}
}

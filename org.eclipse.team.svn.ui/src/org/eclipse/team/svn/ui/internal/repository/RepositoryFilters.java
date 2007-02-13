package org.eclipse.team.svn.ui.internal.repository;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;

public class RepositoryFilters {
	public static final ViewerFilter FOLDERS_ONLY = new ViewerFilter() {
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            return !(element instanceof ISVNRemoteFile);
        }
    };
}

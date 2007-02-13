/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
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

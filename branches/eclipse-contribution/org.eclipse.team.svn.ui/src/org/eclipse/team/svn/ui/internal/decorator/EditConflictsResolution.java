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
package org.eclipse.team.svn.ui.internal.decorator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.actions.EditConflictsAction;
import org.eclipse.ui.IMarkerResolution;

public class EditConflictsResolution implements IMarkerResolution {

    public EditConflictsResolution() {
        super();
    }

    public String getLabel() {
        return Policy.bind("EditConflicts.Label"); //$NON-NLS-1$
    }

    public void run(IMarker marker) {
        new EditConflictsAction((IFile)marker.getResource()).run(null);
    }

}

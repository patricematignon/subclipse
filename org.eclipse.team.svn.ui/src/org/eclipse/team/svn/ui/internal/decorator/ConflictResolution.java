package org.eclipse.team.svn.ui.internal.decorator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.actions.EditConflictsAction;
import org.eclipse.ui.IMarkerResolution;

public class ConflictResolution implements IMarkerResolution {

    public ConflictResolution() {
        super();
    }

    public String getLabel() {
        return Policy.bind("SyncAction.conflicts"); //$NON-NLS-1$
    }

    public void run(IMarker marker) {
        new EditConflictsAction((IFile)marker.getResource()).run(null);
    }

}

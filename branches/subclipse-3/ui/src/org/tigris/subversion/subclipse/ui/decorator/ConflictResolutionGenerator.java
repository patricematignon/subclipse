package org.tigris.subversion.subclipse.ui.decorator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class ConflictResolutionGenerator implements IMarkerResolutionGenerator2 {

    public ConflictResolutionGenerator() {
        super();
    }

    public boolean hasResolutions(IMarker marker) {
        return true;
    }

    public IMarkerResolution[] getResolutions(IMarker marker) {
        ConflictResolution conflictResolution = new ConflictResolution();
        ConflictResolution[] conflictResolutions = { conflictResolution };
        return conflictResolutions;
    }

}

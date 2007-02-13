package org.eclipse.team.svn.ui.internal.decorator;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.internal.SVNException;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;
import org.eclipse.team.svn.ui.internal.operations.ResolveOperation;
import org.eclipse.ui.IMarkerResolution;

public class MarkAsResolvedResolution implements IMarkerResolution {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolution#getLabel()
     */
    public String getLabel() {
        return Policy.bind("MarkResolved.label"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
     */
    public void run(IMarker marker) {
        try {
			new ResolveOperation(null, new IResource[] {marker.getResource()}).run(new NullProgressMonitor());
        } catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof SVNException) {
				SVNUIPlugin.log((SVNException)e.getTargetException());
			} else {
				SVNUIPlugin.log(IStatus.ERROR, e.getMessage(), e);
			}
        } catch (InterruptedException e) {
			SVNUIPlugin.log(IStatus.ERROR, e.getMessage(), e);
        }
    }

}

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
package org.eclipse.team.svn.core.internal.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNAnnotations;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNRevision;
import org.eclipse.team.svn.core.internal.ISVNRemoteFile;
import org.eclipse.team.svn.core.internal.SVNException;

public class GetAnnotationsCommand implements ISVNCommand {

    private ISVNAnnotations annotations;
    private final SVNRevision fromRevision;
    private final SVNRevision toRevision;
    private final ISVNRemoteFile remoteFile;
    
    public GetAnnotationsCommand(ISVNRemoteFile remoteFile, SVNRevision fromRevision, SVNRevision toRevision) {
        this.remoteFile = remoteFile;
        this.fromRevision = fromRevision;
        this.toRevision = toRevision;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.svn.core.internal.commands.ISVNCommand#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws SVNException {
        try {
            monitor.beginTask(null, 100);
            annotations = remoteFile.getRepository().getSVNClient().annotate(remoteFile.getUrl(), fromRevision, toRevision);
//            annotations = remoteFile.getAnnotations(monitor);
            monitor.worked(100);
        } catch (SVNClientException e) {
            throw SVNException.wrapException(e);
        } finally {
            monitor.done();
        }
    }
    
    public ISVNAnnotations getAnnotations() {
        return annotations;
    }

}
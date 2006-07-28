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
package org.tigris.subversion.subclipse.ui.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.subversion.client.SVNStatusKind;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;

public final class ResourceWithStatusUtil {
	private ResourceWithStatusUtil() {
	}
	
	public static String getStatus(IResource resource) {
	    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        String result = null;
	       try {
	           LocalResourceStatus status = svnResource.getStatus();
		       if (status.isTextConflicted())
		           result = Policy.bind("CommitDialog.conflicted"); //$NON-NLS-1$
		       else	            
	            if (status.isAdded())
                   result = Policy.bind("CommitDialog.added"); //$NON-NLS-1$
               else
               if (status.isDeleted())
                   result = Policy.bind("CommitDialog.deleted"); //$NON-NLS-1$
               else
        	   if (status.isMissing())
        		   result = Policy.bind("CommitDialog.missing"); //$NON-NLS-1$
        	   else
        	   if (status.isReplaced())
        		   result = Policy.bind("CommitDialog.replaced"); //$NON-NLS-1$
        	   else
               if (status.isTextModified())
                   result = Policy.bind("CommitDialog.modified"); //$NON-NLS-1$				           
               else
               if (!status.isManaged())
                   result = Policy.bind("CommitDialog.unversioned"); //$NON-NLS-1$
               else
                   result = ""; //$NON-NLS-1$
			} catch (TeamException e) {
			    result = ""; //$NON-NLS-1$
			}                   
	    return result;
    }
	
	public static String getPropertyStatus(IResource resource) {
	    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        String result = null;
	       try {
	            LocalResourceStatus status = svnResource.getStatus();
	            if (status.isPropConflicted())
	                result = Policy.bind("CommitDialog.conflicted"); //$NON-NLS-1$		            
	            else if ((svnResource.getStatus() != null) &&
	                (svnResource.getStatus().getPropStatus() != null) &&
	                (svnResource.getStatus().getPropStatus().equals(SVNStatusKind.MODIFIED)))
	                result = Policy.bind("CommitDialog.modified"); //$NON-NLS-1$		
                else
                    result = ""; //$NON-NLS-1$
			} catch (TeamException e) {
			    result = ""; //$NON-NLS-1$
			}                   
	    return result;
    }
}

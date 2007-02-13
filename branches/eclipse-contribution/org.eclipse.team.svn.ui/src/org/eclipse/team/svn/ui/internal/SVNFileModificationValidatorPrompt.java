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
package org.eclipse.team.svn.ui.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.internal.resources.ISVNFileModificationValidatorPrompt;
import org.eclipse.team.svn.ui.internal.dialogs.LockDialog;

public class SVNFileModificationValidatorPrompt implements ISVNFileModificationValidatorPrompt {
    private String comment;
    private boolean stealLock;
    private boolean success;
    private IFile[] files;
    
    public boolean prompt(IFile[] lockFiles, Object context) {
        if (context == null) {
            comment = "";
            stealLock = false;
            return true;
        }
        this.files = lockFiles;
        success = false;
		SVNUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
			    LockDialog lockDialog = new LockDialog(Display.getCurrent().getActiveShell(), files);
                if (lockDialog.open() != LockDialog.CANCEL) {
                    success = true;
                    comment = lockDialog.getComment();
                    stealLock = lockDialog.isStealLock();
                }
			}
		});        
		return success;
   }

    public String getComment() {
        return comment;
    }

    public boolean isStealLock() {
        return stealLock;
    }

}

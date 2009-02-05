/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/
package org.eclipse.team.svn.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.subversion.client.SVNUrl;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.ui.internal.Policy;
import org.eclipse.team.svn.ui.internal.util.ListContentProvider;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * Dialog that ask the user to give the root repository url of the given url 
 */
public class ChooseRootUrlDialog extends ListDialog {
	private SVNUrl url;
    
	/**
     * 
	 * @param parent
     * @param url : the url from which we want to get the root url 
	 */
	public ChooseRootUrlDialog(Shell parent, SVNUrl url) {
		super(parent);
        this.url = url;
        
        List list = new ArrayList();
        
        // we want the user can select "no root url", ie a blank url
        list.add(""); // we cannot add null, we would have a NullPointerException //$NON-NLS-1$
        SVNUrl possibleRoot = this.url;
        while (possibleRoot != null) {
            list.add(possibleRoot);
            possibleRoot = possibleRoot.getParent();
        }        
        
        setTitle(Policy.bind("ChooseRootUrlDialog.rootUrlDialogTitle")); //$NON-NLS-1$
        setAddCancelButton(true);
        setLabelProvider(new LabelProvider());
        setMessage(Policy.bind("ChooseRootUrlDialog.chooseRootUrl")); //$NON-NLS-1$
        setContentProvider(new ListContentProvider());
        setInput(list);
	}
    
    /**
     * get the chosen root url 
     * @return
     */
    public SVNUrl getRootUrl() {
        Object result = getResult()[0];
        if ("".equals(result)) { //$NON-NLS-1$
        	return null;
        } else {
        	return (SVNUrl)result;
        }
    }
    
    
}
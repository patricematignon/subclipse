/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal.util;

public class LinkList {
    private int[][] linkRanges;
    private String[] urls;

    public LinkList(int[][] linkRanges, String[] urls) {
        super();
        this.linkRanges = linkRanges;
        this.urls = urls;
    }
    
    public boolean isLinkAt(int offset) {
    	for (int i = 0; i < linkRanges.length; i++){
    		if (offset >= linkRanges[i][0] && offset < linkRanges[i][0] + linkRanges[i][1]) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public String getLinkAt(int offset) {
    	for (int i = 0; i < linkRanges.length; i++){
    		if (offset >= linkRanges[i][0] && offset < linkRanges[i][0] + linkRanges[i][1]) {
    			return urls[i];
    		}
    	}
    	return null;
    }

    public int[][] getLinkRanges() {
        return linkRanges;
    }
    public void setLinkRanges(int[][] linkRanges) {
        this.linkRanges = linkRanges;
    }
    public String[] getUrls() {
        return urls;
    }
    public void setUrls(String[] urls) {
        this.urls = urls;
    }
}
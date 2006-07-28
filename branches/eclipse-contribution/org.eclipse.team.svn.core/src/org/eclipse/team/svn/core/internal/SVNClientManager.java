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
package org.eclipse.team.svn.core.internal;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.plugin.SVNClientPlugin;

/**
 * Handles the creation of SVNClients
 * 
 * @author Cedric Chabanois (cchab at tigris.org) 
 */
public class SVNClientManager {
    private File configDir = null;
    private boolean fetchChangePathOnDemand = true;
    
    public void startup(IProgressMonitor monitor) throws CoreException {
    }
    
    
	public void shutdown(IProgressMonitor monitor) throws CoreException {
	}

 	/**
	 * @param configDir The configDir to set.
	 */
	public void setConfigDir(File configDir) {
		this.configDir = configDir;
	}
    
    /**
     * @return a new ISVNClientAdapter depending on the client interface
     * @throws SVNClientException
     */
    public ISVNClientAdapter createSVNClient() throws SVNException {
        try {
        	ISVNClientAdapter svnClient = SVNClientPlugin.getClientAdapter();
        	if (configDir != null) {
        		svnClient.setConfigDirectory(configDir);
        	} 
        	if (SVNProviderPlugin.getPlugin().getSvnPromptUserPassword() != null)
        	    svnClient.addPasswordCallback(SVNProviderPlugin.getPlugin().getSvnPromptUserPassword());
        	return svnClient;
        } catch (Exception e) {
        	throw SVNException.wrapException(e);
        }
    }    
    
    public ISVNClientAdapter[] getSVNClientAdapters() throws SVNException {
    	try {
    		ISVNClientAdapter[] clients = SVNClientPlugin.getClientAdapters();
    		return clients;
    	} catch (Exception e) {
        	throw SVNException.wrapException(e);
    	}
    }

	/**
	 * @return Returns the fetchChangePathOnDemand.
	 */
	public boolean isFetchChangePathOnDemand() {
		return fetchChangePathOnDemand;
	}
	/**
	 * @param fetchChangePathOnDemand The fetchChangePathOnDemand to set.
	 */
	public void setFetchChangePathOnDemand(
			boolean fetchChangePathOnDemand) {
		this.fetchChangePathOnDemand = fetchChangePathOnDemand;
	}


	public void setSvnClientInterface(String clientInterface) {
		SVNClientPlugin.setDefaultAdapter(clientInterface);
		
	}
	
}

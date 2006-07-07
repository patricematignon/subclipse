/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.eclipse.team.svn.core.internal;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientException;
import org.eclipse.subversion.client.SVNClientPlugin;

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

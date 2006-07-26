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
package org.tigris.subversion.svnclientadapter.javasvn;

import java.io.File;

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;
import org.tigris.subversion.svnclientadapter.javahl.AbstractJhlClientAdapter;
import org.tigris.subversion.svnclientadapter.javahl.JhlNotificationHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

/**
 * The JavaSVN Adapter works by providing an implementation of the
 * JavaHL SVNClientInterface.  This allows to provide a common
 * JavaHL implementation (AbstractJhlClientAdapter) where the specific
 * adapters just need to initialize the correct underlying classes.
 *
 */
public class JavaSvnClientAdapter extends AbstractJhlClientAdapter {

    protected JavaSvnClientAdapter() {
        svnClient = SVNClientImpl.newInstance();
        notificationHandler = new JhlNotificationHandler();
        svnClient.notification2(notificationHandler);        
        svnClient.setPrompt(new DefaultPromptUserPassword());
    }

    public void createRepository(File path, String repositoryType)
            throws SVNClientException {
    	if (REPOSITORY_FSTYPE_BDB.equalsIgnoreCase(repositoryType))
    		throw new SVNClientException("JavaSVN only supports fsfs repository type.");
    	try {
    		boolean force = false;
    		boolean enableRevisionProperties = false;
			SVNRepositoryFactory.createLocalRepository(path, enableRevisionProperties, force);
		} catch (SVNException e) {
            notificationHandler.logException(e);
            throw new SVNClientException(e);
		}
     }
    
 
    public void addPasswordCallback(ISVNPromptUserPassword callback) {
        if (callback != null) {
	        JavaSvnPromptUserPassword prompt = new JavaSvnPromptUserPassword(callback);
	        this.setPromptUserPassword(prompt);
        }
    }
    
    public boolean statusReturnsRemoteInfo() {
        return true;
    }
    
    public long[] commitAcrossWC(File[] paths, String message, boolean recurse,
            boolean keepLocks, boolean atomic) throws SVNClientException {
        try {
        	String messageString = (message == null) ? "" : message;
            notificationHandler.setCommand(ISVNNotifyListener.Command.COMMIT);
            String[] files = new String[paths.length];
            String commandLine = "commit -m \""+messageString+"\"";
            if (!recurse)
                commandLine+=" -N";
            if (keepLocks)
                commandLine+=" --no-unlock";

            for (int i = 0; i < paths.length; i++) {
                files[i] = fileToSVNPath(paths[i], false);
                commandLine+=" "+ files[i];
            }
            notificationHandler.logCommandLine(commandLine);
			notificationHandler.setBaseDir();

            long[] newRev = ((SVNClientImpl)svnClient).commit(files, messageString, recurse, keepLocks, atomic);
            return newRev;
        } catch (ClientException e) {
            notificationHandler.logException(e);
            throw new SVNClientException(e);
        }

     }
    
    public boolean canCommitAcrossWC() {
        return true;
    }

    /**
     * Returns the status of files and directory recursively.
     * Overrides method from parent class to work around JavaSVN bug when status on resource within ignored folder
     * does not yield any status. 
     *
     * @param path File to gather status.
     * @param descend get recursive status information
     * @param getAll get status information for all files
     * @param contactServer contact server to get remote changes
     *  
     * @return a Status
     * @throws SVNClientException
     */
    public ISVNStatus[] getStatus(File path, boolean descend, boolean getAll, boolean contactServer) throws SVNClientException {
    	//Call the standard status first.
    	ISVNStatus[] statuses = super.getStatus(path, descend, getAll, contactServer);
    	//If status call return empty array it is either correct - the getAll was not specified and there's not
    	//interesting status in WC, or it is the bug on getting status on unversioned with ignored.
    	if (statuses.length == 0) {
    		if (getAll) {
    			//If the getAll was called and it returned nothing, it is probably the bug case
    			return new ISVNStatus[] { new SVNStatusUnversioned(path) };    			
    		} else {
    			//If the getAll was not called, we have to find out, so let's call it again with getAll set.
    			ISVNStatus[] reCheckStatuses = super.getStatus(path, false, true, false);
    			if (reCheckStatuses.length == 0) {
        			//If event after getAll the result is empty, we assume it's the bug.
    				return new ISVNStatus[] { new SVNStatusUnversioned(path) };
    			} else {
    				//The result after getAll was not empty, so the very first empty result was OK, there's nothing interesting in WC.
    				return new ISVNStatus[0];
    			}
    		}
    	} else {
    		return statuses;
    	}
    }
   
}

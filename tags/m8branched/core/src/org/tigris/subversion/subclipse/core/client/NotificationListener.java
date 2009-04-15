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
package org.tigris.subversion.subclipse.core.client;

import java.io.*;

import org.tigris.subversion.subclipse.core.*;
import org.tigris.subversion.svnclientadapter.*;

/**
 * This class listen to notifications from svnClientAdapter and redirect them to the console listener
 */
public class NotificationListener implements ISVNNotifyListener {

	private IConsoleListener consoleListener;
    private static NotificationListener instance;

    /*
     * private contructor 
     */
    private NotificationListener() {
        consoleListener = SVNProviderPlugin.getPlugin().getConsoleListener();     
    }
    
    /**
     * Returns the singleton instance
     */
    public static NotificationListener getInstance() {      
        if(instance==null) {
            instance = new NotificationListener();
        }
        return instance;
    }

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logCommandLine(java.lang.String)
	 */
	public void logCommandLine(String commandLine) {
        if (consoleListener != null) {
	        consoleListener.logCommandLine(commandLine);
        }
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logCompleted(java.lang.String)
	 */
	public void logCompleted(String message) {
		if (consoleListener != null) {
        	consoleListener.logCompleted(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logError(java.lang.String)
	 */
	public void logError(String message) {
		if (consoleListener != null) {
        	consoleListener.logError(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#logMessage(java.lang.String)
	 */
	public void logMessage(String message) {
		if (consoleListener != null) {
			consoleListener.logMessage(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#onNotify(java.lang.String, org.tigris.subversion.svnclientadapter.SVNNodeKind)
	 */
	public void onNotify(File path, SVNNodeKind kind) {
		if (consoleListener != null) {
			consoleListener.onNotify(path,kind);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNNotifyListener#setCommand(int)
	 */
	public void setCommand(int command) {
		if (consoleListener != null) {
        	consoleListener.setCommand(command);
		}
	}

}
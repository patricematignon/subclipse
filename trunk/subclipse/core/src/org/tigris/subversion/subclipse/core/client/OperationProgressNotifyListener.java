/******************************************************************************
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at the following URL:
 * http://www.eclipse.org/legal/cpl-v10.html
 * Copyright(c) 2003-2005 by the authors indicated in the @author tags.
 *
 * All Rights are Reserved by the various authors.
 *******************************************************************************/
package org.tigris.subversion.subclipse.core.client;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * ISVNNotifyListener implementation which intercepts the logMessage() method
 * and used it to display the operation progress within the supplied progress monitor.
 * 
 * @author Martin Letenay (letenay at tigris.org)
 */
public class OperationProgressNotifyListener extends ISVNNotifyAdapter {

	private IProgressMonitor monitor = null;
	
	public OperationProgressNotifyListener(final IProgressMonitor monitor)
	{
		super();
		this.monitor = monitor;
	}
	
	/**
	 * Display the log the message in the progress monitor and increase the progress by 1
	 */
	public void logMessage(String message) {
		if (monitor != null)
		{
		    monitor.worked(1);
		    monitor.subTask(message);
		}
	}
	
	/**
	 * The operation was completed, clear the progress' subTask.
	 */
	public void logCompleted(String message) {
		if (monitor != null)
		{
		    monitor.subTask(" ");
		}		
	}

}

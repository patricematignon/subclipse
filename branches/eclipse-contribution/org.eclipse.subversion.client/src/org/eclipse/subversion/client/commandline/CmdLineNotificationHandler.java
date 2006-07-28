/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.subversion.client.commandline;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.subversion.client.ISVNNotifyListener;
import org.eclipse.subversion.client.SVNNotificationHandler;

/**
 * Command line specific extension to generic notification handler
 * 
 * @author Cédric Chabanois (cchabanois@ifrance.com)
 */
public class CmdLineNotificationHandler extends SVNNotificationHandler {

	/**
	 * Log the supplied command line exception as Error
	 * @param e an exception to log
	 */
    public void logException(CmdLineException e) {
        StringTokenizer st = new StringTokenizer(e.getMessage(), Helper.NEWLINE);
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            for (Iterator it = notifylisteners.iterator(); it.hasNext();) {
                ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
                listener.logError(line);
            }
        }
    }

}

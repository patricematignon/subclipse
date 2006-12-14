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
package org.tigris.subversion.svnclientadapter.commandline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * SvnCommandLine subclass for handling command with multiple arguments (thus multiple answers)
 * @see org.tigris.subversion.svnclientadapter.commandline.SvnCommandLine 
 */
public class SvnMultiArgCommandLine extends SvnCommandLine {

    protected List revs = new ArrayList();
	
	SvnMultiArgCommandLine(String svnPath,CmdLineNotificationHandler notificationHandler) {
		super(svnPath,notificationHandler);
	}	

    /*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.commandline.CommandLine#notifyFromSvnOutput(java.lang.String)
	 */
	protected void notifyFromSvnOutput(String svnOutput) {
		// we call the super implementation : handles logMessage and logCompleted
		super.notifyMessagesFromSvnOutput(svnOutput);

		if (parseSvnOutput) {
			// we parse the svn output
			CmdLineNotify notify = new CmdLineNotify() {
		
				public void onNotify(
						String path,
				        int action,
				        int kind,
				        String mimeType,
				        int contentState,
				        int propState,
				        long revision) {
					// we only call notifyListenersOfChange and logRevision
					// logMessage and logCompleted have already been called
					if (path != null) {
						notificationHandler.notifyListenersOfChange(path);
					}
					SvnMultiArgCommandLine.this.revs.add(new Long(revision));
					if (revision != SVNRevision.SVN_INVALID_REVNUM) {
						notificationHandler.logRevision(revision, path);
					}
				}
				
			};
			
		
			try {
				svnOutputParser.addListener(notify);
				svnOutputParser.parse(svnOutput);
			} finally {
				svnOutputParser.removeListener(notify);			
			}
		}
		
	}

	/**
	 * get the revisions notified for latest command. If an error occured, the value
	 * of revisions must be ignored
	 * @return Returns the revisions.
	 */
	public long[] getRevisions() {
		long[] result = new long[revs.size()];
		int i = 0;
		for (Iterator iter = revs.iterator(); iter.hasNext();) {
			result[i] = ((Long )iter.next()).longValue();
			i++;
		}
		return result;
	}

}

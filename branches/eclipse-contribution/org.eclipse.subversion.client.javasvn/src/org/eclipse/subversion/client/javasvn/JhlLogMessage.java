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
package org.eclipse.subversion.client.javasvn;

import java.util.Date;

import org.eclipse.subversion.client.ISVNLogMessage;
import org.eclipse.subversion.client.ISVNLogMessageChangePath;
import org.eclipse.subversion.client.SVNRevision;
import org.tigris.subversion.javahl.LogMessage;

/**
 * A JavaHL based implementation of {@link ISVNLogMessage}.
 * Actually just an adapter from {@link org.tigris.subversion.javahl.LogMessage}
 *  
 * @author philip schatz
 */
public class JhlLogMessage implements ISVNLogMessage {

	private LogMessage _m;

	/**
	 * Constructor
	 * @param msg
	 */
	public JhlLogMessage(LogMessage msg) {
		super();
		_m = msg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNLogMessage#getRevision()
	 */
	public SVNRevision.Number getRevision() {
		return (SVNRevision.Number)JhlConverter.convert(_m.getRevision());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNLogMessage#getAuthor()
	 */
	public String getAuthor() {
		return _m.getAuthor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNLogMessage#getDate()
	 */
	public Date getDate() {
		return _m.getDate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.ISVNLogMessage#getMessage()
	 */
	public String getMessage() {
		return _m.getMessage();
	}

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.ISVNLogMessage#getChangedPaths()
     */
    public ISVNLogMessageChangePath[] getChangedPaths() {
    	return JhlConverter.convert(_m.getChangedPaths());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getMessage();
    }

}

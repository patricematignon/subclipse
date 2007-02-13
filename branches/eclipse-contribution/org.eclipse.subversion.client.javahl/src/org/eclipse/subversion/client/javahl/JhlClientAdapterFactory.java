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
package org.eclipse.subversion.client.javahl;

import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientAdapterFactory;
import org.eclipse.subversion.client.SVNClientException;

/**
 * Concrete implementation of SVNClientAdapterFactory for javahl interface.
 * To register this factory, just call {@link JhlClientAdapterFactory#setup()} 
 */
public class JhlClientAdapterFactory extends SVNClientAdapterFactory {
	
	/** Client adapter implementation identifier */
    public static final String JAVAHL_CLIENT = "javahl";

	/**
	 * Private constructor.
	 * Clients are expected the use {@link #createSVNClientImpl()}, res.
	 * ask the {@link SVNClientAdapterFactory}
	 */
    private JhlClientAdapterFactory() {
    	super();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.subversion.client.SVNClientAdapterFactory#createSVNClientImpl()
	 */
	protected ISVNClientAdapter createSVNClientImpl() {
		return new JhlClientAdapter();
	}

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.SVNClientAdapterFactory#getClientType()
     */
    protected String getClientType() {
        return JAVAHL_CLIENT;
    }
    
    /**
     * Setup the client adapter implementation and register it in the adapters factory
     * @throws SVNClientException
     */
    public static void setup() throws SVNClientException {
    	JhlClientAdapter adapter = new JhlClientAdapter();
        if (!adapter.isAvailable()) {
        	adapter = null;
        	throw new SVNClientException("Javahl client adapter is not available");
        }
        adapter = null;
    	SVNClientAdapterFactory.registerAdapterFactory(new JhlClientAdapterFactory());
    }
  
}

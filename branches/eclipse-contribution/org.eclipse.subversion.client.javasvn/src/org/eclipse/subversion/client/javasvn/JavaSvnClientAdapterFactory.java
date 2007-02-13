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
package org.eclipse.subversion.client.javasvn;

import org.eclipse.subversion.client.ISVNClientAdapter;
import org.eclipse.subversion.client.SVNClientAdapterFactory;
import org.eclipse.subversion.client.SVNClientException;

/**
 * Concrete implementation of SVNClientAdapterFactory for JavaSVN interface.
 * To register this factory, just call {@link JavaSvnClientAdapterFactory#setup()} 
 */
public class JavaSvnClientAdapterFactory extends SVNClientAdapterFactory {
	
	/** Client adapter implementation identifier */
    public static final String JAVASVN_CLIENT = "javasvn";
    
	/**
	 * Private constructor.
	 * Clients are expected the use {@link #createSVNClientImpl()}, res.
	 * ask the {@link SVNClientAdapterFactory}
	 */
    private JavaSvnClientAdapterFactory() {
    	super();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.subversion.client.SVNClientAdapterFactory#createSVNClientImpl()
	 */
	protected ISVNClientAdapter createSVNClientImpl() {
		return new JavaSvnClientAdapter();
	}

    /* (non-Javadoc)
     * @see org.eclipse.subversion.client.SVNClientAdapterFactory#getClientType()
     */
    protected String getClientType() {
        return JAVASVN_CLIENT;
    }
    
    /**
     * Setup the client adapter implementation and register it in the adapters factory
     * @throws SVNClientException
     */
    public static void setup() throws SVNClientException {
    	SVNClientAdapterFactory.registerAdapterFactory(new JavaSvnClientAdapterFactory());
    }
  
}

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
package org.tigris.subversion.svnclientadapter.javahl;

import org.tigris.subversion.javahl.SVNClient;
import org.tigris.subversion.javahl.SVNClientInterface;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Concrete implementation of SVNClientAdapterFactory for javahl interface.
 * To register this factory, just call {@link JhlClientAdapterFactory#setup()} 
 */
public class JhlClientAdapterFactory extends SVNClientAdapterFactory {
    
    private static boolean availabilityCached = false;
    private static boolean available;
	private static StringBuffer javaHLErrors = new StringBuffer("Failed to load JavaHL Library.\nThese are the errors that were encountered:\n");
	
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
	 * @see org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory#createSVNClientImpl()
	 */
	protected ISVNClientAdapter createSVNClientImpl() {
		return new JhlClientAdapter();
	}

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory#getClientType()
     */
    protected String getClientType() {
        return JAVAHL_CLIENT;
    }
    
    /**
     * Setup the client adapter implementation and register it in the adapters factory
     * @throws SVNClientException
     */
    public static void setup() throws SVNClientException {
        if (!isAvailable()) {
        	throw new SVNClientException("Javahl client adapter is not available");
        }
        
    	SVNClientAdapterFactory.registerAdapterFactory(new JhlClientAdapterFactory());
    }
    
    public static boolean isAvailable() {
    	if (!availabilityCached) {
    		Class c = null;
    		try {
    			// load a JavaHL class to see if it is found.  Do not use SVNClient as
    			// it will try to load native libraries and we do not want that yet
    			c = Class.forName("org.tigris.subversion.javahl.ClientException");
    			if (c == null)
    				return false;
    		} catch (Throwable t) {
    			availabilityCached = true;
    			return false;
    		}
    		// if library is already loaded, it will not be reloaded

    		//workaround to solve Subclipse ISSUE #83
    		// we will ignore these exceptions to handle scenarios where
    		// javaHL was built diffently.  Ultimately, if javaHL fails to load
    		// because of a problem in one of these libraries the proper behavior
    		// will still occur -- meaning JavaHL adapter is disabled.
    		if(isOsWindows()) {
    			StringBuffer bdbErrors = new StringBuffer();
    			boolean bdbLoaded = false;
    			try {
    				System.loadLibrary("libapr");
    			} catch (Exception e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			} catch (UnsatisfiedLinkError e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			}
    			try {
    				System.loadLibrary("libapriconv");
    			} catch (Exception e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			} catch (UnsatisfiedLinkError e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			}
    			try {
    				System.loadLibrary("libeay32");
    			} catch (Exception e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			} catch (UnsatisfiedLinkError e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			}
    			try {
    				System.loadLibrary("libdb44");
    				bdbLoaded = true;
    			} catch (Exception e) {
    				bdbErrors.append(e.getMessage()).append("\n");
    			} catch (UnsatisfiedLinkError e) {
    				bdbErrors.append(e.getMessage()).append("\n");
    			}
    			try {
    				System.loadLibrary("libdb43");
    				bdbLoaded = true;
    			} catch (Exception e) {
    				bdbErrors.append(e.getMessage()).append("\n");
    			} catch (UnsatisfiedLinkError e) {
    				bdbErrors.append(e.getMessage()).append("\n");
    			}
    			if (!bdbLoaded) {
    				javaHLErrors.append(bdbErrors.toString());
    			}
    			try {
    				System.loadLibrary("ssleay32");
    			} catch (Exception e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			} catch (UnsatisfiedLinkError e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			}
    			try {
    				System.loadLibrary("libaprutil");
    			} catch (Exception e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			} catch (UnsatisfiedLinkError e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			}
    			try {
    				System.loadLibrary("intl3_svn");
    			} catch (Exception e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			} catch (UnsatisfiedLinkError e) {
    				javaHLErrors.append(e.getMessage()).append("\n");
    			}
    		}
    		//workaround to solve Subclipse ISSUE #83
    		available = false;
    		try {
    			/*
    			 * see if the user has specified the fully qualified path to the native
    			 * library
    			 */
    			try
    			{
    				String specifiedLibraryName =
    					System.getProperty("subversion.native.library");
    				if(specifiedLibraryName != null) {
    					System.load(specifiedLibraryName);
    					available = true;
    				}
    			}
    			catch(UnsatisfiedLinkError ex)
    			{
    				javaHLErrors.append(ex.getMessage()).append("\n");
    			}
    			if (!available) {
    				/*
    				 * first try to load the library by the new name.
    				 * if that fails, try to load the library by the old name.
    				 */
    				try
    				{
    					System.loadLibrary("libsvnjavahl-1");
    				}
    				catch(UnsatisfiedLinkError ex)
    				{
    					javaHLErrors.append(ex.getMessage() + "\n");
    					try
    					{
    						System.loadLibrary("svnjavahl-1");
    					}
    					catch (UnsatisfiedLinkError e)
    					{
    						javaHLErrors.append(e.getMessage()).append("\n");
    						System.loadLibrary("svnjavahl");
    					}
    				}

    				available = true;
    			}
    		} catch (Exception e) {
    			available = false;
    			javaHLErrors.append(e.getMessage()).append("\n");
    		} catch (UnsatisfiedLinkError e) {
    			available = false;
    			javaHLErrors.append(e.getMessage()).append("\n");
    		} finally {
    			availabilityCached = true;
    		}
    		if (!available) {
    			String libraryPath = System.getProperty("java.library.path");
    			if (libraryPath != null)
    				javaHLErrors.append("java.library.path = " + libraryPath);
    			// System.out.println(javaHLErrors.toString());
    		} else {
    			// At this point, the library appears to be available, but
    			// it could be a 1.2.x version of JavaHL.  We have to try
    			// to execute a 1.3.x method to be sure.
    			try {
	                SVNClientInterface svnClient = new SVNClient();
	                String dirname = svnClient.getAdminDirectoryName();
    				// to remove compiler warning about dirname not being read
    				if (dirname != null)  
    					available = true;
    			} catch (UnsatisfiedLinkError e) {
    				available = false;
    				javaHLErrors.append("Incompatible JavaHL library loaded.  1.3.x or later required.");
    			}
    		}
    	}

    	return available;
    }
    
	/**
	 * Answer whether running on Windows OS.
	 * (Actual code extracted from org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS)
	 * (For such one simple method it does make sense to introduce dependency on whole commons-lang.jar)
	 * @return true when the underlying 
	 */
	public static boolean isOsWindows()
	{
        try {
            return System.getProperty("os.name").startsWith("Windows");
        } catch (SecurityException ex) {
            // we are not allowed to look at this property
            return false;
        }
	}
	
    /**
     * @return an error string describing problems during loading platform native libraries (if any)
     */
    public static String getLibraryLoadErrors() {
        if (isAvailable())
            return "";
        else
            return javaHLErrors.toString();
    }

}
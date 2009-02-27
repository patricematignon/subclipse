/**
 * @copyright
 * ====================================================================
 * Copyright (c) 2003-2004 CollabNet.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://subversion.tigris.org/license-1.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 *
 * This software consists of voluntary contributions made by many
 * individuals.  For exact contribution history, see the revision
 * history and logs, available at http://subversion.tigris.org/.
 * ====================================================================
 * @endcopyright
 */
package org.tigris.subversion.svnclientadapter.basictests;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.testUtils.OneTest;
import org.tigris.subversion.svnclientadapter.testUtils.SVNTest;


public class MkdirTest extends SVNTest {
    private static final Logger log = Logger.getLogger(MkdirTest.class.getName());
    
    
    /**
     * test basic SVNClient.mkdir with url parameter functionality
     * @throws Throwable
     */
    public void testBasicMkdirUrl() throws Throwable
    {
        // build the test setup.
        OneTest thisTest = new OneTest("basicMkdirUrl",getGreekTestConfig());

        // create Y and Y/Z directories in the repository
        client.mkdir(new SVNUrl(thisTest.getUrl() + "/Y"),"log_msg");
        client.mkdir(new SVNUrl(thisTest.getUrl() + "/Y/Z"),"log_msg");

        // add the new directories the expected working copy layout
        thisTest.getExpectedWC().addItem("Y", null);
        thisTest.getExpectedWC().setItemWorkingCopyRevision("Y", 3); // should be 2 ... ?
        thisTest.getExpectedWC().addItem("Y/Z", null);
        thisTest.getExpectedWC().setItemWorkingCopyRevision("Y/Z", 3);

        // update the working copy
        assertEquals("wrong revision from update",3,
                client.update(thisTest.getWCPath(), SVNRevision.HEAD, true));

        // check the status of the working copy
        thisTest.checkStatusesExpectedWC();
    }

	/**
	 * Test if Subversion will detect the change of a file to a direcory
	 * @throws Throwable
	 */
	public void testBasicNodeKindChange() throws Throwable
	{
	    // create working copy
	    OneTest thisTest = new OneTest("basicNodeKindChange",getGreekTestConfig());
	
	    //  remove A/D/gamma
	    client.remove(new File[] {new File(thisTest.getWCPath()+"/A/D/gamma")}, false);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/gamma", SVNStatusKind.DELETED);
	
	    // check the working copy status
	    thisTest.checkStatusesExpectedWC();
	
	    try
	    {
	        // creating a directory in the place of the deleted file should
	        // fail
	        client.mkdir(new File(thisTest.getWCPath()+"/A/D/gamma"));
	        fail("can change node kind");
	    }
	    catch(SVNClientException e)
	    {
			log.log(Level.FINE, e.getMessage(), e);
	    }
	
	    // check the working copy status
	    thisTest.checkStatusesExpectedWC();
	
	    // commit the deletion
	    assertEquals("wrong revision number from commit",2,
	            client.commit(new File[]{thisTest.getWCPath()},"log message",
	                    true));
	    thisTest.getExpectedWC().removeItem("A/D/gamma");
	
	    // check the working copy status
	    thisTest.checkStatusesExpectedWC();
	
	    try
	    {
	        // creating a directory in the place of the deleted file should
	        // still fail
	        client.mkdir(new File(thisTest.getWCPath()+"/A/D/gamma"));
	        fail("can change node kind");
	    }
	    catch(SVNClientException e)
	    {
			log.log(Level.FINE, e.getMessage(), e);
	    }
	
	    // check the working copy status
	    thisTest.checkStatusesExpectedWC();
	
	    // update the working copy
	    client.update(thisTest.getWCPath(), SVNRevision.HEAD, true);
	
	    // check the working copy status
	    thisTest.checkStatusesExpectedWC();
	
	    // now creating the directory should succeed
	    client.mkdir(new File(thisTest.getWCPath()+"/A/D/gamma"));
	    thisTest.getExpectedWC().addItem("A/D/gamma", null);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/gamma", SVNStatusKind.ADDED);
	
	    // check the working copy status
	    thisTest.checkStatusesExpectedWC();
	}
    
    
}
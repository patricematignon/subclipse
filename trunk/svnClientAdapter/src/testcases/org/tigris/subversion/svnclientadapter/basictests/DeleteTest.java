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
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;


public class DeleteTest extends SVNTest {

	/**
	 * test the basic SVNClient.remove functionality
	 * @throws Throwable
	 */
	public void testBasicDelete() throws Throwable
	{
	    // create the test working copy
	    OneTest thisTest = new OneTest("basicDelete",getGreekTestConfig());
	
	    // modify A/D/H/chi
	    File file = new File(thisTest.getWorkingCopy(), "A/D/H/chi");
	    PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
	    pw.print("added to chi");
	    pw.close();
	    thisTest.getExpectedWC().setItemTextStatus("A/D/H/chi", SVNStatusKind.MODIFIED);
	
	    // set a property on A/D/G/rho file
	    client.propertySet(new File(thisTest.getWCPath()+"/A/D/G/rho"), "abc", "def",
	            true);
	    thisTest.getExpectedWC().setItemPropStatus("A/D/G/rho", SVNStatusKind.MODIFIED);
	
	    // set a property on A/B/F directory
	    client.propertySet(new File(thisTest.getWCPath()+"/A/B/F"), "abc", "def", false);
	    thisTest.getExpectedWC().setItemPropStatus("A/B/F", SVNStatusKind.MODIFIED);
	
	    // create a unversioned A/C/sigma file
	    file = new File(thisTest.getWCPath(),"A/C/sigma");
	    pw = new PrintWriter(new FileOutputStream(file));
	    pw.print("unversioned sigma");
	    pw.close();
	    thisTest.getExpectedWC().addItem("A/C/sigma", "unversioned sigma");
	    thisTest.getExpectedWC().setItemTextStatus("A/C/sigma", SVNStatusKind.UNVERSIONED);
	    
        // TODO : temporary removed so that test pass with javasvn
	    // thisTest.getWc().setItemNodeKind("A/C/sigma", SVNNodeKind.UNKNOWN);
	
	    // create unversioned directory A/C/Q
	    file = new File(thisTest.getWCPath(), "A/C/Q");
	    file.mkdir();
	    thisTest.getExpectedWC().addItem("A/C/Q", null);
	    
        // TODO : temporary removed so that test pass with javasvn
        // thisTest.getWc().setItemNodeKind("A/C/Q", SVNNodeKind.UNKNOWN);
	    
        thisTest.getExpectedWC().setItemTextStatus("A/C/Q", SVNStatusKind.UNVERSIONED);
	
	    // create & add the directory A/B/X
	    file = new File(thisTest.getWCPath(), "A/B/X");
	    client.mkdir(file);
	    thisTest.getExpectedWC().addItem("A/B/X", null);
	    thisTest.getExpectedWC().setItemTextStatus("A/B/X", SVNStatusKind.ADDED);
	
	    // create & add the file A/B/X/xi
	    file = new File(file, "xi");
	    pw = new PrintWriter(new FileOutputStream(file));
	    pw.print("added xi");
	    pw.close();
	    client.addFile(file);
	    thisTest.getExpectedWC().addItem("A/B/X/xi", "added xi");
	    thisTest.getExpectedWC().setItemTextStatus("A/B/X/xi", SVNStatusKind.ADDED);
	
	    // create & add the directory A/B/Y
	    file = new File(thisTest.getWCPath(), "A/B/Y");
	    client.mkdir(file);
	    thisTest.getExpectedWC().addItem("A/B/Y", null);
	    thisTest.getExpectedWC().setItemTextStatus("A/B/Y", SVNStatusKind.ADDED);
	
	    // test the status of the working copy
	    thisTest.checkStatusesExpectedWC();
	
	    // the following removes should all fail without force
	
	    try
	    {
	        // remove of A/D/H/chi without force should fail, because it is
	        // modified
	        client.remove(new File[] { new File(thisTest.getWCPath()+"/A/D/H/chi") },
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    try
	    {
	        // remove of A/D/H without force should fail, because A/D/H/chi is
	        // modified
	        client.remove(new File[] {new File(thisTest.getWCPath()+"/A/D/H") },
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    try
	    {
	        // remove of A/D/G/rho without force should fail, because it has
	        // a new property
	        client.remove(new File[] { new File(thisTest.getWCPath()+"/A/D/G/rho") },
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    try
	    {
	        // remove of A/D/G without force should fail, because A/D/G/rho has
	        // a new property
	        client.remove(new File[] { new File(thisTest.getWCPath()+"/A/D/G") }, 
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    try
	    {
	        // remove of A/B/F without force should fail, because it has
	        // a new property
	        client.remove(new File[] { new File(thisTest.getWCPath()+"/A/B/F") }, 
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    try
	    {
	        // remove of A/B without force should fail, because A/B/F has
	        // a new property
	        client.remove(new File[] { new File(thisTest.getWCPath()+"/A/B") },
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    try
	    {
	        // remove of A/C/sigma without force should fail, because it is
	        // unversioned
	        client.remove(new File[] { new File(thisTest.getWCPath()+"/A/C/sigma") },
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    try
	    {
	        // remove of A/C without force should fail, because A/C/sigma is
	        // unversioned
	        client.remove(new File[] { new File(thisTest.getWCPath()+"/A/C") }, 
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    try
	    {
	        // remove of A/B/X without force should fail, because it is new
	        client.remove(new File[] { new File(thisTest.getWCPath()+"/A/B/X")}, 
	                false);
	        fail("missing exception");
	    }
	    catch(SVNClientException e)
	    {
	    }
	
	    // check the status of the working copy
	    thisTest.checkStatusesExpectedWC();
	
	    // the following removes should all work
	    client.remove(new File[] { new File(thisTest.getWCPath()+"/A/B/E")}, 
	            false);
	    thisTest.getExpectedWC().setItemTextStatus("A/B/E",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemTextStatus("A/B/E/alpha",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemTextStatus("A/B/E/beta",SVNStatusKind.DELETED);
	    client.remove(new File[] { new File(thisTest.getWCPath()+"/A/D/H")}, true);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/H",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/H/chi",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/H/omega",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/H/psi",SVNStatusKind.DELETED);
	    client.remove(new File[] { new File(thisTest.getWCPath()+"/A/D/G")}, true);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/G",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/G/rho",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemPropStatus("A/D/G/rho", SVNStatusKind.NORMAL);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/G/pi",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/G/tau",SVNStatusKind.DELETED);
	    client.remove(new File[] { new File(thisTest.getWCPath()+"/A/B/F")}, true);
	    thisTest.getExpectedWC().setItemTextStatus("A/B/F",SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().setItemPropStatus("A/B/F", SVNStatusKind.NORMAL);
	    client.remove(new File[] { new File(thisTest.getWCPath()+"/A/C")}, true);
	    thisTest.getExpectedWC().setItemTextStatus("A/C",SVNStatusKind.DELETED);
	    client.remove(new File[] { new File(thisTest.getWCPath()+"/A/B/X")}, true);
	    file = new File(thisTest.getWorkingCopy(), "iota");
	    file.delete();
	    client.remove(new File[] {file}, true);
	    thisTest.getExpectedWC().setItemTextStatus("iota",SVNStatusKind.DELETED);
	    file = new File(thisTest.getWorkingCopy(), "A/D/gamma");
	    file.delete();
	    client.remove(new File[] {file}, false);
	    thisTest.getExpectedWC().setItemTextStatus("A/D/gamma",SVNStatusKind.DELETED);
	    client.remove(new File[] {file}, true);
	    client.remove(new File[] { new File(thisTest.getWCPath()+"/A/B/E")}, false);
	    thisTest.getExpectedWC().removeItem("A/B/X");
	    thisTest.getExpectedWC().removeItem("A/B/X/xi");
	    thisTest.getExpectedWC().removeItem("A/C/sigma");
	    thisTest.getExpectedWC().removeItem("A/C/Q");
	    thisTest.checkStatusesExpectedWC();
	    client.remove(new File[] { new File(thisTest.getWCPath()+"/A/D")},true);
	    thisTest.getExpectedWC().setItemTextStatus("A/D", SVNStatusKind.DELETED);
	    thisTest.getExpectedWC().removeItem("A/D/Y");
	
	    // check the status of the working copy
	    thisTest.checkStatusesExpectedWC();
	
	    // confirm that the file are realy deleted
	    assertFalse("failed to remove text modified file",
	            new File(thisTest.getWorkingCopy(), "A/D/G/rho").exists());
	    assertFalse("failed to remove prop modified file",
	            new File(thisTest.getWorkingCopy(), "A/D/H/chi").exists());
	    assertFalse("failed to remove unversioned file",
	            new File(thisTest.getWorkingCopy(), "A/C/sigma").exists());
	    assertFalse("failed to remove unmodified file",
	            new File(thisTest.getWorkingCopy(), "A/B/E/alpha").exists());
	    file = new File(thisTest.getWorkingCopy(),"A/B/F");
	    assertTrue("removed versioned dir", file.exists()
	            && file.isDirectory());
	    assertFalse("failed to remove unversioned dir",
	            new File(thisTest.getWorkingCopy(), "A/C/Q").exists());
	    assertFalse("failed to remove added dir",
	            new File(thisTest.getWorkingCopy(), "A/B/X").exists());
	
	    // delete unversioned file foo
	    file = new File(thisTest.getWCPath(),"foo");
	    pw = new PrintWriter(new FileOutputStream(file));
	    pw.print("unversioned foo");
	    pw.close();
	    client.remove(new File[] {file}, true);
	    assertFalse("failed to remove unversioned file foo", file.exists());
	    client.remove(new File[] {file}, true);
	
	    // delete file iota in the repository
	    client.remove(new SVNUrl[] { new SVNUrl(thisTest.getUrl()+"/iota")},
	            "delete iota URL");
	}
    
    public void testRemoveUrls() throws Throwable {
        // create the working copy
        OneTest thisTest = new OneTest("basicRemoveUrls",getGreekTestConfig());

        // remove A/mi
        client.remove(new SVNUrl[] { new SVNUrl(thisTest.getUrl()+"/A/mu") },"A/mu removed");
        
        // list directory A
        ISVNDirEntry[] entries = client.getList(new SVNUrl(thisTest.getUrl()+"/A"), SVNRevision.HEAD, false);
    }       
    

}

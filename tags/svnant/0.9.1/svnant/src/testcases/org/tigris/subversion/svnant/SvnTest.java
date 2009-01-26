/*
 * Created on 10 mars 2003
 *
 */
package org.tigris.subversion.svnant;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * You can set javahl to true or false in test/build.properties
 * @author C�dric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnTest extends BuildFileTest {
private ISVNClientAdapter svnClient;
private static final String WORKINGCOPY_DIR = "test/svn/workingcopy"; 

    public SvnTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("test/svn/build.xml");
        boolean javahl = true;
        String javahlProp = getProject().getProperty("javahl");
        if (javahlProp != null)
            javahl = getProject().getProperty("javahl").equalsIgnoreCase("true");
        
        if ((javahl) && (SVNClientAdapterFactory.isSVNClientAvailable(SVNClientAdapterFactory.JAVAHL_CLIENT))) {        
    		svnClient = SVNClientAdapterFactory.createSVNClient(SVNClientAdapterFactory.JAVAHL_CLIENT);
        }
        else
            svnClient = SVNClientAdapterFactory.createSVNClient(SVNClientAdapterFactory.COMMANDLINE_CLIENT);
    }

	public void tearDown()
	{
		System.out.print(getLog());
	}

    public void testCheckout() throws SVNClientException {
        executeTarget("testCheckout");

		assertEquals(1,svnClient.getSingleStatus(new File("test/svn/coHEAD/checkoutTest/readme.txt")).getLastChangedRevision().getNumber());
    }
    

	public void testList() throws SVNClientException,MalformedURLException {
       executeTarget("testList");
	   String urlRepos = getProject().getProperty("urlRepos");
	   ISVNDirEntry[] list = svnClient.getList(new SVNUrl(urlRepos+"/listTest"),SVNRevision.HEAD,false);
	   assertTrue(list.length > 0);
	}


    public void testLog() throws SVNClientException {
        executeTarget("testLog");
		String urlRepos = getProject().getProperty("urlRepos");
		ISVNLogMessage[] messages = svnClient.getLogMessages(new File(WORKINGCOPY_DIR+"/logTest/file1.txt"),new SVNRevision.Number(0),SVNRevision.HEAD);
        assertTrue(messages.length > 0);
		assertEquals("logTest directory added to repository",messages[0].getMessage());
    }

    public void testAddCommit() throws SVNClientException {
       executeTarget("testAddCommit");
	   assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/addCommitTest/file0.add")).getLastChangedRevision().getNumber() > 0);
    }

    public void testCopy() throws SVNClientException {
    	executeTarget("testCopy");
		assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/copyTest/copy1")).getLastChangedRevision().getNumber() > 0);
    } 

	public void testDelete() {
		executeTarget("testDelete");
		assertFalse(new File(WORKINGCOPY_DIR+"/deleteTest/deleteFromWorkingCopy/file0.del").exists());
		assertTrue(new File(WORKINGCOPY_DIR+"/deleteTest/deleteFromWorkingCopy/donotdel.txt").exists());
	}

	public void testExport() {
		executeTarget("testExport");
	}
	
	public void testImport() {
		executeTarget("testImport");
	} 

	
	public void testMkdir() throws SVNClientException {
		executeTarget("testMkdir");
		assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/mkdirTest/testMkdir2")).getLastChangedRevision().getNumber() > 0);
    } 
	
	public void testMove() throws SVNClientException {
		executeTarget("testMove");
		assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/moveTest/dir1Renamed")).getLastChangedRevision().getNumber() > 0);
	}
   
    public void testProp() throws SVNClientException {
        executeTarget("testProp");
        ISVNProperty propData = svnClient.propertyGet(new File(WORKINGCOPY_DIR+"/propTest/file.png"),"svn:mime-type");
        assertTrue(propData != null);
        assertEquals("image/png",propData.getValue());
        propData = svnClient.propertyGet(new File(WORKINGCOPY_DIR+"/propTest/file.png"),"myPicture");
        assertTrue(propData != null);
        assertEquals(170,propData.getData().length);

        // we don't test that because propDel does not work property with javahl interface for now        
//        propData = svnClient.propertyGet(new File(WORKINGCOPY_DIR+"/propTest/file.png"),"myProperty");
//        assertTrue(propData == null);
    }

    public void testDiff() {
        executeTarget("testDiff");
        File patchFile = new File(WORKINGCOPY_DIR+"/diffTest/patch.txt");
        assertTrue(patchFile.exists());
        assertTrue(patchFile.length() > 0);
    }
    
    public void testKeywords() throws FileNotFoundException, IOException {
        executeTarget("testKeywords");
        DataInputStream dis = new DataInputStream(new FileInputStream(WORKINGCOPY_DIR+"/keywordsTest/file.txt")); 
        assertEquals("$LastChangedRevision: 1 $",dis.readLine());
        assertEquals("$Author: cedric $",dis.readLine());
        assertEquals("$Id$",dis.readLine());
    }

    public void testUpdate() {
        executeTarget("testUpdate");
    }

    public void testRevert() throws FileNotFoundException, IOException {
        executeTarget("testRevert");
        DataInputStream dis = new DataInputStream(new FileInputStream(WORKINGCOPY_DIR+"/revertTest/file.txt")); 
        assertEquals("first version",dis.readLine());
    }

    public void testCat() throws FileNotFoundException, IOException {
        executeTarget("testCat");
        DataInputStream dis = new DataInputStream(new FileInputStream(WORKINGCOPY_DIR+"/catTest/filecat.txt")); 
        assertEquals("first line",dis.readLine());        
        assertEquals("second line",dis.readLine());
    }

    public void testListener() throws Exception {
        final Set addSet = new HashSet();
        final Set commitSet = new HashSet();
        final Set[] currentSet = new Set[] { null };
        final boolean duplicates = false;

        ISVNNotifyListener listener = new ISVNNotifyListener() {
            public void setCommand(int command) {
                if (command == ISVNNotifyListener.Command.ADD) {
                    currentSet[0] = addSet;
                } else {
                    currentSet[0] = commitSet;
                }
            }
            
            public void logCommandLine(String commandLine) {}
            public void logMessage(String message) {}
            public void logError(String message) {}
            public void logCompleted(String message) {}
  
            public void onNotify(File path, SVNNodeKind kind) {
                currentSet[0].add(path);
            }
        };
        
        Target target = (Target)project.getTargets().get("testListener");
        // first task is "copy", second one is "svn"
        Task task = target.getTasks()[1];
        SvnTask svnTask;
        if (task instanceof UnknownElement) {
            // not sure how ant works but it seems to work using that
            UnknownElement unknownElement = (UnknownElement)task;
            unknownElement.maybeConfigure();
            svnTask = (SvnTask)unknownElement.getTask(); 
        }else {
            svnTask = (SvnTask)task;
        }
 
        svnTask.addNotifyListener(listener);
        executeTarget("testListener");
        
        assertEquals(6,addSet.size()); // 6 for add and 6 for commit        
        assertEquals(6,commitSet.size()); // 6 for add and 6 for commit
        
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest").getCanonicalFile()));
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1").getCanonicalFile()));
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1/file3.txt").getCanonicalFile()));        
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1/file4.txt").getCanonicalFile()));
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/file1.txt").getCanonicalFile()));
        assertTrue(addSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/file2.txt").getCanonicalFile()));

        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest").getCanonicalFile()));
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1").getCanonicalFile()));
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1/file3.txt").getCanonicalFile()));        
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/dir1/file4.txt").getCanonicalFile()));
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/file1.txt").getCanonicalFile()));
        assertTrue(commitSet.contains(new File(WORKINGCOPY_DIR+"/listenerTest/file2.txt").getCanonicalFile()));
        
    }

    public void testIgnore() throws Exception {
        executeTarget("testIgnore");
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/ignoreTest/fileToIgnore.txt")).isIgnored());
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/ignoreTest/dir1/file1.ignore")).isIgnored());
        assertFalse(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/ignoreTest/dir1/file2.donotignore")).isIgnored());
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/ignoreTest/dir1/dir2/file3.ignore")).isIgnored());        
    }


    public void testSingleStatus() throws Exception {
        executeTarget("testStatus");
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/added.txt")).isAdded());
        
        // a resource that does not exist is a non managed resource
        assertFalse(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/fileThatDoesNotExist.txt")).isManaged());
        
        // same test but in a directory that is not versioned
        assertFalse(null,svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir/fileThatDoesNotExist.txt")).isManaged());
        
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/ignored.txt")).isIgnored());    
        
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/committed.txt")).hasRemote());        
        
        assertTrue(svnClient.getSingleStatus(new File(WORKINGCOPY_DIR+"/statusTest/deleted.txt")).isDeleted());

    }
    
    public void testStatus() throws Exception {  
      
		executeTarget("testStatus");
        ISVNStatus[] statuses;  
        // getStatus(File, boolean) does not have the same result with command line interface
        // and svnjavahl for now. svnjavahl does not return ignored files for now 
//        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest"),false);
        // let's verify we don't forget some files (ignored ones for example)
//        assertEquals(8,statuses.length);
        
//        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest"),true);
//        assertEquals(9,statuses.length);

//        statuses = svnClient.getStatus(new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir").getCanonicalFile(),false);
//        assertEquals(1, statuses.length);

        
        statuses = svnClient.getStatus( new File[] {
            new File(WORKINGCOPY_DIR+"/statusTest/added.txt"),
            new File(WORKINGCOPY_DIR+"/statusTest/managedDir/added in managed dir.txt"),
            new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir"),
            new File("nonExistingFile"),
            new File(WORKINGCOPY_DIR+"/statusTest/ignored.txt"),
            new File(WORKINGCOPY_DIR+"/statusTest/nonManaged.dir/statusTest")
            }
        );
        assertEquals(6,statuses.length);
        assertEquals(new File(WORKINGCOPY_DIR+"/statusTest/added.txt").getCanonicalFile(), statuses[0].getFile());
        assertTrue(statuses[0].isAdded());
        
        assertEquals(new File(WORKINGCOPY_DIR+"/statusTest/managedDir/added in managed dir.txt").getAbsoluteFile(), statuses[1].getFile());        
        assertTrue(statuses[1].isManaged());
        assertEquals(SVNNodeKind.FILE,statuses[1].getNodeKind());
        
        assertFalse(statuses[2].isManaged());
        assertEquals(SVNNodeKind.UNKNOWN,statuses[2].getNodeKind());

        assertFalse(statuses[3].isManaged());
        assertEquals(SVNNodeKind.UNKNOWN,statuses[3].getNodeKind());
        
        assertTrue(statuses[4].isIgnored());
        assertEquals(SVNNodeKind.UNKNOWN,statuses[4].getNodeKind()); // an ignored resource is a not versioned one, so its resource kind is UNKNOWN
        
        // make sure that the top most directory is said to be versionned. It is in a directory where there is no
        // .svn directory but it is versionned however. 
        assertTrue(statuses[5].isManaged());
        assertNotNull(statuses[5].getUrl());       
    }

	public void testEntry() throws Exception {
		executeTarget("testEntry");
		String urlRepos = getProject().getProperty("urlRepos");
		ISVNDirEntry dirEntry = svnClient.getDirEntry(new SVNUrl(urlRepos+"/entryTest/"),SVNRevision.HEAD);
		assertNotNull(dirEntry);
		assertEquals(SVNNodeKind.DIR,dirEntry.getNodeKind());
		assertEquals("entryTest",dirEntry.getPath());
	}

    public static void main(String[] args) {
        String[] testCaseName = { SvnTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
        //		junit.ui.TestRunner.main(testCaseName);
    }

}
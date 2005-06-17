package org.tigris.subversion.svnclientadapter;

import junit.framework.TestCase;

/**
 */
public class SVNUrlUtilsTest extends TestCase {

    public void testGetCommonRootUrl() throws Exception {
        SVNUrl[] urls = new SVNUrl[] {
                new SVNUrl("http://svn.collab.net/repos/subclipse/myfile.txt"),
                new SVNUrl("http://svn.collab.net:80/repos/subclipse/myfile2.txt"),
                new SVNUrl("HTTP://svn.collab.net/repos/subclipse/mydir/myfile.txt"),
                new SVNUrl("http://svn.collab.net/repos/subclipse/mydir/mydir2/myfile.txt")
        };
        assertEquals("http://svn.collab.net/repos/subclipse", SVNUrlUtils.getCommonRootUrl(urls).toString());
    }

    public void testGetCommonRootUrlNoRoot() throws Exception {
        SVNUrl[] urls = new SVNUrl[] {
                new SVNUrl("http://svn.collab.net:81/repos/subclipse/myfile.txt"),
                new SVNUrl("http://svn.collab.net:80/repos/subclipse/myfile2.txt"),
                new SVNUrl("HTTP://svn.collab.net/repos/subclipse/mydir/myfile.txt"),
                new SVNUrl("http://svn.collab.net/repos/subclipse/mydir/mydir2/myfile.txt")
        };
        assertEquals(null, SVNUrlUtils.getCommonRootUrl(urls));
    }
    
    public void testRelativePath() throws Exception {
        SVNUrl url = new SVNUrl("http://svn.collab.net:81/repos/subclipse/myfile.txt");
        SVNUrl rootUrl = new SVNUrl("http://svn.collab.net:81/repos");
        assertEquals("subclipse/myfile.txt",SVNUrlUtils.getRelativePath(rootUrl,url));
        assertEquals("",SVNUrlUtils.getRelativePath(rootUrl,rootUrl));
    }
    
    public void testGetUrlFromLocalFileName() throws Exception {
        SVNUrl rootUrl = new SVNUrl("http://svn.collab.net:81/repos/mydir");
        String rootPath = "C:\\Documents and Settings\\User\\My Documents\\Eclipse\\mydir";
        String filePath = "C:\\Documents and Settings\\User\\My Documents\\Eclipse\\mydir\\mydir2\\myFile.txt";
        SVNUrl expected = new SVNUrl("http://svn.collab.net:81/repos/mydir/mydir2/myFile.txt");
        assertEquals(expected,SVNUrlUtils.getUrlFromLocalFileName(filePath, rootUrl, rootPath));

        rootUrl = new SVNUrl("http://svn.collab.net:81/repos/mydir/");
        assertEquals(expected,SVNUrlUtils.getUrlFromLocalFileName(filePath, rootUrl, rootPath));

        rootPath = "C:\\Documents and Settings\\User\\My Documents\\Eclipse\\mydir\\";
        assertEquals(expected,SVNUrlUtils.getUrlFromLocalFileName(filePath, rootUrl, rootPath));
        rootUrl = new SVNUrl("http://svn.collab.net:81/repos/mydir");

        rootPath = "C:/Documents and Settings/User/My Documents/Eclipse/mydir";
        filePath = "C:\\Documents and Settings\\User\\My Documents\\Eclipse\\mydir\\mydir2\\myFile.txt";
        assertEquals(expected,SVNUrlUtils.getUrlFromLocalFileName(filePath, rootUrl, rootPath));

        rootPath = "C:\\Documents and Settings\\User\\My Documents\\Eclipse\\mydir";
        filePath = "C:/Documents and Settings/User/My Documents/Eclipse/mydir/mydir2/myFile.txt";
        assertEquals(expected,SVNUrlUtils.getUrlFromLocalFileName(filePath, rootUrl, rootPath));

        rootPath = "C:/Documents and Settings/User/My Documents/Eclipse/mydir/";
        assertEquals(expected,SVNUrlUtils.getUrlFromLocalFileName(filePath, rootUrl, rootPath));
    }
    
}

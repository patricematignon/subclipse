package org.tigris.subversion.svnclientadapter;

import junit.framework.TestCase;

import org.tigris.subversion.javahl.DirEntry;
import org.tigris.subversion.javahl.Notify;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.SVNClient;
import org.tigris.subversion.javahl.Revision.Kind;


public class SVNClientAdapterTest extends TestCase
{
    
    public void testList()throws Exception{
        
        SVNClient client = new SVNClient();
        client.notification(new Notify(){

            public void onNotify(String arg0, int arg1, int arg2, String arg3, int arg4, int arg5, long arg6)
            {
                System.out.println("I was notified: "+arg0);
                
            }
            
        });
        DirEntry[] entries = client.list("http://svn.collab.net:81/repos/subclipse/trunk/subclipse", new Revision(Kind.head), true);
        assertNotNull(entries);
        assertTrue(entries.length > 0);
    }

}

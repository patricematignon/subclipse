/*
 * Created on 26 f�vr. 2004
 */
package org.tigris.subversion.svnant;

import org.apache.tools.ant.BuildFileTest;

import org.junit.After;
import org.junit.Before;

/**
 * to run this test, you should use keychain (or ssh-agent) otherwise the password will be asked many times 
 * 
 * @author C�dric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class SvnSshTest extends BuildFileTest {

    @Before
    public void setUp() {
        configureProject( "test/svnssh/build.xml" );
    }

    @After
    public void tearDown() {
        System.out.print( getLog() );
    }

    public void testSvnservePasswdSucceed() throws Exception {
        executeTarget( "testPasswdSucceed" );
    }

}

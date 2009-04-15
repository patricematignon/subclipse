package org.tigris.subversion.svnclientadapter.commandline;

import java.util.Calendar;

import junit.framework.TestCase;

public class HelperTest extends TestCase {

	public void testConvertXMLDate() throws Exception {
		
		// before patch from Jennifer Bevan, svnClientAdapter was incorrectly
		// setting dates at 12:xx PM to 12:xx AM  
	    Calendar cal = Calendar.getInstance();
	    cal.set(2003, 0, 10,23,21,54);
		assertEquals(cal.getTime().toString(), Helper.convertXMLDate("2003-01-10T23:21:54.831325Z").toString());
		cal.set(2003, 0, 11,12,01,06);
		assertEquals(cal.getTime().toString(), Helper.convertXMLDate("2003-01-11T12:01:06.649052Z").toString());
		cal.set(2003, 0,11,0,4,33);
		assertEquals(cal.getTime().toString(), Helper.convertXMLDate("2003-01-11T00:04:33.633658Z").toString());
		cal.set(2003,0,11,12,13,31);
		assertEquals(cal.getTime().toString(), Helper.convertXMLDate("2003-01-11T12:13:31.499504Z").toString());
	}

}
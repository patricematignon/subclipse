import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.swing.ImageIcon;

import org.junit.Test;


public class DiningPhilosophersTest {

	// It shouldn't be possible to create an icon by reading from a null path
	@Test( expected=NullPointerException.class )
	public void testCreateAppletImageIconNullPathNullDescription() {
		ImageIcon iic = DiningPhilosophers.createAppletImageIcon( null, null );
		fail( "An icon can't be created from a null path.  ImageIcon = " + iic.toString() );
	}
	
	// A null ImageIcon should be created if the file is non-existent
	@Test
	public void testCreateAppletImageIconNonexistentFileNullDescription() {
		ImageIcon iic = DiningPhilosophers.createAppletImageIcon( "nonexistent.xxx", null );
		assertNull( "Non-null ImageIcon created.", iic );
	}

	// A directory-listing should not be considered a valid image file for an ImageIcon 
	@Test( expected=IllegalArgumentException.class )
	public void testCreateAppletImageIconEmptyPathEmptyDescription() {
		@SuppressWarnings("unused")
		ImageIcon iic = DiningPhilosophers.createAppletImageIcon( "", "" );
		fail( "An icon can't be created from a directory listing." );
	}

}

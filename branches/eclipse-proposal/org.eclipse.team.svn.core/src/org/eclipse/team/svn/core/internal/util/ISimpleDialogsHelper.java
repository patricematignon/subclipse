
/**
 *
 * This interface exists to provide the UI package a way to pass dialogs 
 * helpers to the subclipse core package. 
 * 
 * @author Magnus Naeslund (mag@kite.se)
 * 
 */

package org.eclipse.team.svn.core.internal.util;

/**
 * 
 * @author mag
 * @see org.eclipse.team.svn.ui.internal.util.SimpleDialogsHelper
 * @see org.eclipse.team.svn.core.internal.SVNProviderPlugin#getSimpleDialogsHelper()
 *
 */

public interface ISimpleDialogsHelper {
	
	/**
	 * 
	 * @param title
	 * @param question
	 * @param yesIsDefault
	 * @return true if the user pressed yes
	 * 
	 */
	
	public boolean promptYesNo(String title, String question, boolean yesIsDefault);
	public boolean promptYesCancel(String title, String question, boolean yesIsDefault);
	
	
}

package org.eclipse.team.svn.ui.internal.util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.core.internal.util.ISimpleDialogsHelper;
import org.eclipse.team.svn.ui.internal.SVNUIPlugin;

/**
 *	
 *	This class is a dialog helper class for the core package.
 *  It's made availabe for core thru SVNProviderPlugin.getSimpleDialogsHelper().
 *	New simple dialogs should be added here so the minimum of glue is needed for
 *  every new dialog. Remember to update the ISimpleDialogsHelper interface. 
 * 
 * @author Magnus Naeslund (mag@kite.se)
 * @see org.eclipse.team.svn.core.internal.util.ISimpleDialogsHelper 
 * @see org.eclipse.team.svn.core.internal.SVNProviderPlugin#getSimpleDialogsHelper()
 */

public class SimpleDialogsHelper implements ISimpleDialogsHelper {

	public boolean promptYesNo(String title, String question, boolean yesIsDefault) {
		MessageDialogRunnable mdr = new MessageDialogRunnable(
				null,
                title,
                null,
                question,
                MessageDialog.QUESTION,
                new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
                yesIsDefault ? 0 : 1);
        SVNUIPlugin.getStandardDisplay().syncExec(mdr);
		return mdr.getResult() == 0;
	}

	public boolean promptYesCancel(String title, String question, boolean yesIsDefault) {
		MessageDialogRunnable mdr = new MessageDialogRunnable(
				null,
                title,
                null,
                question,
                MessageDialog.QUESTION,
                new String[] {IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL},
                yesIsDefault ? 0 : 1);
        SVNUIPlugin.getStandardDisplay().syncExec(mdr);
		return mdr.getResult() == 0;
	}
	
	/**
	 * 
	 * This should be reused for all MessageDialog type of dialogs.
	 * 
	 * @author mag
	 *
	 */
	
	private static class MessageDialogRunnable implements Runnable {
		final Shell shell;
		final String title, message;
		final Image image;
		final int imageType, defaultButton;
		final String buttonLabels[];
		int result;
		
		/**
		 * 
		 * @param shell if null, it's Display.getCurrent().getActiveShell()
		 * @param title
		 * @param image can be null
		 * @param message
		 * @param imageType
		 * @param buttonLabels
		 * @param defaultButton
		 */

		MessageDialogRunnable(Shell shell, String title, Image image, String message, int imageType, String buttonLabels[], int defaultButton){
			this.shell = shell;
			this.title = title;
			this.image = image;
			this.message = message;
			this.imageType = imageType;
			this.buttonLabels = buttonLabels;
			this.defaultButton = defaultButton;  
		}
		
		public void run() {
			result = new MessageDialog(
					shell != null ? shell : Display.getCurrent().getActiveShell(),
					title, image, message, imageType,
					buttonLabels, defaultButton).open();
		}
		
		public int getResult(){
			return result;
		}
	}

}

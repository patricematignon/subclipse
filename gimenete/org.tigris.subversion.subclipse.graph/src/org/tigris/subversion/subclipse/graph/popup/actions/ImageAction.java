package org.tigris.subversion.subclipse.graph.popup.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.tigris.subversion.subclipse.graph.dialogs.SaveImageDialog;
import org.tigris.subversion.subclipse.graph.editors.RevisionGraphEditor;

public class ImageAction extends Action {
	private RevisionGraphEditor editor;

	public ImageAction(RevisionGraphEditor editor) {
		super();
		this.editor = editor;
		setText("Save image to file...");
	}

	public void run() {
		SaveImageDialog dialog = new SaveImageDialog(Display.getDefault().getActiveShell(), editor);
		dialog.open();
	}

}

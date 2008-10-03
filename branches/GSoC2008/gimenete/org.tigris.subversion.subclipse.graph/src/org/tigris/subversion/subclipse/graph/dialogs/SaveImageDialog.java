package org.tigris.subversion.subclipse.graph.dialogs;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.Figure;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.tigris.subversion.subclipse.graph.Activator;
import org.tigris.subversion.subclipse.graph.ImageFactory;
import org.tigris.subversion.subclipse.graph.editors.GraphEditPart;
import org.tigris.subversion.subclipse.graph.editors.RevisionGraphEditor;

public class SaveImageDialog extends TrayDialog {
	private RevisionGraphEditor editor;
	private Combo fileTypeCombo;
	private Text fileText;
	private Button browseButton;
	private int lastOutput;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	
	private Button okButton;
	
	private static final int BMP = 0;
	private static final int JPEG = 1;
//	private static final int GIF = 2;
//	private static final int ICO = 3;
//	private static final int PNG = 4;
	private final static String LAST_OUTPUT = "SaveImageDialog.lastOutput";

	public SaveImageDialog(Shell parentShell, RevisionGraphEditor editor) {
		super(parentShell);
		this.editor = editor;
		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.RESIZE);
		try {
			lastOutput = settings.getInt(LAST_OUTPUT);
		} catch (Exception e) {}
	}

	protected Control createDialogArea(Composite parent) {
		getShell().setText("Save Image to File");
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label typeLabel = new Label(composite, SWT.NONE);
		typeLabel.setText("Save as file type:");
		fileTypeCombo = new Combo(composite, SWT.READ_ONLY);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		fileTypeCombo.setLayoutData(gd);
		fileTypeCombo.add("BMP");
		fileTypeCombo.add("JPEG");
//		fileTypeCombo.add("GIF");
//		fileTypeCombo.add("ICO");
//		fileTypeCombo.add("PNG");

		Label fileLabel = new Label(composite, SWT.NONE);
		fileLabel.setText("Save to file:");
		fileText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		gd.widthHint = 300;
		fileText.setLayoutData(gd);
		browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				selectFile();
			}			
		});
		
		switch (lastOutput) {
		case BMP:
			fileTypeCombo.setText("BMP");
			break;
		case JPEG:
			fileTypeCombo.setText("JPEG");
			break;
//		case GIF:
//			fileTypeCombo.setText("GIF");
//			break;
//		case ICO:
//			fileTypeCombo.setText("ICO");
//			break;
//		case PNG:
//			fileTypeCombo.setText("PNG");
//			break;				
		default:
			fileTypeCombo.setText("BMP");
			break;
		}
		
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				okButton.setEnabled(canFinish());
			}		
		};
		fileText.addModifyListener(modifyListener);
		
		FocusListener focusListener = new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				((Text)e.getSource()).selectAll();
			}
			public void focusLost(FocusEvent e) {
				((Text)e.getSource()).setText(((Text)e.getSource()).getText());
			}					
		};
		fileText.addFocusListener(focusListener);
		
		return composite;
	}
	
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			okButton = button; 
			okButton.setEnabled(false);
		}
        return button;
    }
	
	protected void okPressed() {
		settings.put(LAST_OUTPUT, fileTypeCombo.getSelectionIndex());
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				GraphEditPart editPart = (GraphEditPart)editor.getViewer().getContents();
				Figure figure = (Figure)editPart.getFigure();
				int imageType = ImageFactory.BMP;
				switch (fileTypeCombo.getSelectionIndex()) {
				case BMP:
					imageType = ImageFactory.BMP;
					break;
				case JPEG:
					imageType = ImageFactory.JPEG;
					break;	
//				case PNG:
//					imageType = ImageFactory.PNG;
//					break;					
//				case ICO:
//					imageType = ImageFactory.ICO;
//					break;					
//				case GIF:
//					imageType = ImageFactory.GIF;
//					break;
				default:
					break;
				}
				byte[] image = ImageFactory.createImage(editor.getViewer(), figure, imageType);
				try {
					String extension;
					if (fileTypeCombo.getText().equals("JPEG")) extension = "jpg";
					else extension = fileTypeCombo.getText().toLowerCase();
					String fileName = null;
					if (!fileText.getText().trim().endsWith("." + extension))
						fileName = fileText.getText().trim() + "." + extension;
					else
						fileName = fileText.getText().trim();
					File file = new File(fileName);
					FileOutputStream out = new FileOutputStream(file);
					out.write(image);
					try {
						out.close();
					} catch (Exception e1) {}
				} catch (Exception e) {
					MessageDialog.openError(getShell(), "Save Image to File", e.getMessage());
					return;
				}
			}		
		});
		super.okPressed();
	}

	private boolean canFinish() {
		if (fileText.getText().trim().length() == 0) return false;
		File file = new File(fileText.getText().trim());
		return isValidFile(file);
	}
	
	private boolean isValidFile(File file) {
		if (!file.isAbsolute()) return false;
		if (file.isDirectory()) return false;
		File parent = file.getParentFile();
		if (parent==null) return false;
		if (!parent.exists()) return false;
		if (!parent.isDirectory()) return false;
		return true;
	}
	
	private void selectFile() {
		String extension;
		if (fileTypeCombo.getText().equals("JPEG")) extension = "jpg";
		else extension = fileTypeCombo.getText().toLowerCase();
		FileDialog d = new FileDialog(getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
		d.setText("Save Revision Graph As");
		d.setFileName(editor.getEditorInput().getName() + "." + extension);
		String file = d.open();
		if(file!=null) {
			IPath path = new Path(file);
			fileText.setText(path.toOSString());
		}						
	}

}

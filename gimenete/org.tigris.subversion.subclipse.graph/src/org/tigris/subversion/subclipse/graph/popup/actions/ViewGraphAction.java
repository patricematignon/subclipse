package org.tigris.subversion.subclipse.graph.popup.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.actions.SVNAction;
import org.tigris.subversion.sublicpse.graph.cache.Cache;
import org.tigris.subversion.sublicpse.graph.cache.Node;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class ViewGraphAction extends SVNAction {

	/*
	 * @see SVNAction#executeIAction)
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IResource resource = getSelectedResources()[0];
				IFile file = (IFile) resource;
				
				Cache cache = null;
				try {
					ISVNClientAdapter client = SVNProviderPlugin.getPlugin().createSVNClient();
					ISVNInfo info = client.getInfoFromWorkingCopy(file.getRawLocation().toFile());
					long revision = info.getRevision().getNumber();
					String path = info.getUrl().toString().substring(info.getRepository().toString().length());
					
					cache = getCache(file, info.getUuid());
					
					// update the cache
					long latestRevisionStored = cache.getLatestRevision();
					SVNRevision latest = null;
					if(latestRevisionStored == 0)
						latest = SVNRevision.START;
					else
						latest = new SVNRevision.Number(latestRevisionStored+1);
					
					try {
						ISVNLogMessage[] messages = client.getLogMessages(info.getRepository(),
								latest, SVNRevision.HEAD);
						// printLogMessages(messages);
						cache.update(messages);
					} catch(SVNClientException e) {
						System.err.println("cache may be up to date "+e.getMessage()+" "+e.getClass());
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					// query information
					Long fileId = cache.getFileId(path, revision);
					List nodes = cache.getNodes(fileId.longValue());
					// printNodes(nodes);
					showGraph(nodes, path);

				} catch (Exception e) {
					e.printStackTrace();
					return;
				} finally {
					if(cache != null)
						cache.close();
					// TODO: clean up ISVNClientAdapter ?
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	
	private Cache getCache(IFile file, String uuid) {
		File database = file.getWorkspace().getRoot().getRawLocation().toFile();
		database = new File(database, ".metadata");
		database = new File(database, ".plugins");
		database = new File(database, "org.tigris.subversion.subclipse.graph");
		database = new File(database, uuid);
		return new Cache(database);
	}
	
	private void showGraph(List nodes, String path) {
		final Shell shell = new Shell();
		shell.setSize(400, 400);
		shell.setText("Revision graph. "+path);
		LightweightSystem lws = new LightweightSystem(shell);
		Figure contents = new Figure();
		XYLayout contentsLayout = new XYLayout();
		contents.setLayoutManager(contentsLayout);
		
		Map branches = new HashMap();
		Map figures = new HashMap();
		Map previousFigure = new HashMap();

		int i=0;
		for (Iterator iterator = nodes.iterator(); iterator
				.hasNext();) {
			Node node = (Node) iterator.next();
			NodeFigure figure = new NodeFigure(node.getRevision(), node.getAuthor(), node.getRevisionDate());
			figures.put(node.getRevision()+"@"+node.getPath(), figure);
			
			BranchFigure branch = (BranchFigure) branches.get(node.getPath());
			if(branch == null) {
				branch = new BranchFigure(node.getPath());
				contentsLayout.setConstraint(branch, new Rectangle(10+branches.keySet().size()*170, 10, 150, 30));
				branches.put(node.getPath(), branch);
				contents.add(branch);
				previousFigure.put(node.getPath(), branch);
			}
			Rectangle r = (Rectangle) contentsLayout.getConstraint(branch);
			contentsLayout.setConstraint(figure, new Rectangle(r.x, 50+60*i, 150, 50));
			contents.add(figure);

			ChopboxAnchor sourceAnchor = null;
			if(node.getCopySrcPath() == null) {
				sourceAnchor = new ChopboxAnchor((Figure) previousFigure.get(node.getPath()));
			} else {
				Figure source = (Figure) figures.get(node.getCopySrcRevision()+"@"+node.getCopySrcPath());
				if(source != null) {
					sourceAnchor = new ChopboxAnchor(source);
				} else {
					long rev = node.getCopySrcRevision();
					do { // FIXME: this is very ugly
						source = (Figure) figures.get(rev+"@"+node.getCopySrcPath());
						rev--;
					} while(source == null && rev >= 0);
					if(source != null) {
						sourceAnchor = new ChopboxAnchor(source);
					} else {
						sourceAnchor = new ChopboxAnchor((Figure) previousFigure.get(node.getCopySrcPath()));
					}
				}
			}
			if(sourceAnchor != null) {
				PolylineConnection c = new PolylineConnection();
				 c.setConnectionRouter(new ManhattanConnectionRouter());
				ChopboxAnchor targetAnchor = new ChopboxAnchor(figure);
				c.setSourceAnchor(sourceAnchor);
				c.setTargetAnchor(targetAnchor);
				contents.add(c);
			}
			
			previousFigure.put(node.getPath(), figure);
			
			i++;
		}
		
		lws.setContents(contents);
		Label label = new Label("Foo bar");
		label.setLocation(new Point(200, 200));
		shell.open();
		Display d = shell.getDisplay();
		while (!shell.isDisposed())
			while (!d.readAndDispatch())
				d.sleep();
	}
	
	private void printLogMessages(ISVNLogMessage[] messages) {
		for (int i = 0; i < messages.length; i++) {
			ISVNLogMessage message = messages[i];
			System.out.println(MessageFormat.format("{0} rev {1} on {3} message: {2}",
					new Object[]{ message.getAuthor(),
					message.getRevision(),
					message.getMessage(),
					new SimpleDateFormat("dd/MM/yyyy mm:ss").format(message.getDate())}));
			ISVNLogMessageChangePath[] changedPaths = message.getChangedPaths();
			for (int j = 0; j < changedPaths.length; j++) {
				ISVNLogMessageChangePath cp = changedPaths[j];
				System.out.println(MessageFormat.format("{0} {1}",
						new Object[]{ Character.toString(cp.getAction()), cp.getPath()}));
			}
		}
	}
	
	private void printNodes(List nodes) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy mm:ss");
		for (Iterator iterator = nodes.iterator(); 
				iterator.hasNext();) {
			Node node = (Node) iterator.next();
			System.out.println(MessageFormat.format("{0} rev {1} on {3} {2}",
					new Object[]{ node.getAuthor(),
					new Long(node.getRevision()),
					node.getPath(),
					dateFormat.format(node.getRevisionDate())}));
		}
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		return resources.length == 1 && resources[0].getType() == IResource.FILE;
	}
	/**
	 * @see org.tigris.subversion.subclipse.ui.actions.SVNAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("ShowHistoryAction.showHistory"); //$NON-NLS-1$
	}

	/*
	 * @see org.tigris.subversion.subclipse.ui.actions.ReplaceableIconAction#getImageId()
	 */
	protected String getImageId() {
		return ISVNUIConstants.IMG_MENU_SHOWHISTORY;
	}
	
	public static Label createLabel(String text, Font font) {
		Label label = new Label(text);
		label.setFont(font);
		return label;
	}

} class NodeFigure extends Figure {
	
	public static Color classColor = new Color(null,255,255,206);
	
	public NodeFigure(long revision, String author, Date date) {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);	
		setBorder(new LineBorder(ColorConstants.black,1));
		setBackgroundColor(classColor);
		setOpaque(true);

		Font revisionFont = new Font(null, "Arial", 10, SWT.BOLD);
		Font authorFont = new Font(null, "Arial", 10, SWT.BOLD);
		Font dateFont = new Font(null, "Arial", 10, SWT.ITALIC);

		add(ViewGraphAction.createLabel(Long.toString(revision), revisionFont));
		add(ViewGraphAction.createLabel(author, authorFont));
		add(ViewGraphAction.createLabel(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date), dateFont));
	}
	
} class BranchFigure extends Figure {
	
	public static Color classColor = new Color(null,255,255,206);
	
	public BranchFigure(String path) {
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);	
		setBorder(new LineBorder(ColorConstants.black,1));
		setBackgroundColor(classColor);
		setOpaque(true);

		Label label = new Label(path);
		add(label);	
	}
}
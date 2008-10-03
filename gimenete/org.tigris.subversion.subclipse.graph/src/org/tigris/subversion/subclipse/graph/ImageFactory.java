package org.tigris.subversion.subclipse.graph;

import java.io.ByteArrayOutputStream;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class ImageFactory {
	public static final int BMP = SWT.IMAGE_BMP;
	public static final int BMP_RLE = SWT.IMAGE_BMP_RLE;
	public static final int GIF = SWT.IMAGE_GIF;
	public static final int ICO = SWT.IMAGE_ICO;
	public static final int JPEG = SWT.IMAGE_JPEG;
	public static final int PNG = SWT.IMAGE_PNG;
	
	public static byte[] createImage(EditPartViewer viewer, IFigure figure, int format) {
		Device device = viewer.getControl().getDisplay();
		Rectangle r = figure.getBounds();
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		Image image = null;
		GC gc = null;
		Graphics g = null;
		try {
			image = new Image(device, r.width, r.height);
			gc = new GC(image);
			g = new SWTGraphics(gc);
			g.translate(r.x * -1, r.y * -1);
			figure.paint(g);
			ImageLoader imageLoader = new ImageLoader();	
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(result, format);			
		} finally {
			if (g != null) g.dispose();
			if (gc != null) gc.dispose();
			if (image != null) image.dispose();
		}
		return result.toByteArray();
	}
}

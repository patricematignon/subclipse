package org.tigris.subversion.subclipse.ui.sync;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;


class Decoration implements IDecoration {
	public String prefix, suffix;
	public ImageDescriptor overlay;

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addPrefix(java.lang.String)
	 */
	public void addPrefix(String prefix) {
		this.prefix = prefix;
	}
	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addSuffix(java.lang.String)
	 */
	public void addSuffix(String suffix) {
		this.suffix = suffix;
	}
	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void addOverlay(ImageDescriptor overlay) {
		this.overlay = overlay;
	}
}

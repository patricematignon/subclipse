/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C�dric Chabanois (cchabanois@ifrance.com) - modified for Subversion 
 *******************************************************************************/

package org.tigris.subversion.subclipse.ui.comments;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class used to read comment history using SAX 
 */
class CommentHistoryContentHandler extends DefaultHandler {

	private StringBuffer buffer;
	private Vector comments;
	public CommentHistoryContentHandler() {
	}

	/**
	 * @see ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] chars, int startIndex, int length) {
		if (buffer == null) return;
		buffer.append(chars, startIndex, length);
	}

	/**
	 * @see ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			Attributes atts) {
		if (localName.equals(CommentsManager.ELEMENT_COMMIT_COMMENT)) {
			buffer = new StringBuffer();
			return;
		} 
		if (localName.equals(CommentsManager.ELEMENT_COMMIT_HISTORY)) {
			comments = new Vector(CommentsManager.MAX_COMMENTS);
			return;
		}
	}
	
	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName) {
		if (localName.equals(CommentsManager.ELEMENT_COMMIT_COMMENT)) {
			comments.add(buffer.toString());
			buffer = null;
			return;
		} 
		if (localName.equals(CommentsManager.ELEMENT_COMMIT_HISTORY)) {
            CommentsManager.previousComments = new String[comments.size()];
			comments.copyInto(CommentsManager.previousComments);
			return;
		} 
	}
}

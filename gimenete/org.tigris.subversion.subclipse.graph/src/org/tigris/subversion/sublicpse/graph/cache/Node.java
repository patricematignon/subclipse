package org.tigris.subversion.sublicpse.graph.cache;

import java.sql.Timestamp;
import java.text.MessageFormat;

public class Node {
	
	private long revision;
	private String author;
	private Timestamp revisionDate;
	private String message;
	private String path;
	private char action;
	private long copySrcRevision;
	private String copySrcPath;

//	public Node(long revision, String author, Timestamp revisionDate,
//			String message, String path, char action, long copySrcRevision,
//			String copySrcPath) {
//		this.revision = revision;
//		this.author = author;
//		this.revisionDate = revisionDate;
//		this.message = message;
//		this.path = path;
//		this.action = action;
//		this.copySrcRevision = copySrcRevision;
//		this.copySrcPath = copySrcPath;
//	}
	
	public Node() {
	}
	
	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Timestamp getRevisionDate() {
		return revisionDate;
	}

	public void setRevisionDate(Timestamp revisionDate) {
		this.revisionDate = revisionDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public char getAction() {
		return action;
	}

	public void setAction(char action) {
		this.action = action;
	}

	public long getCopySrcRevision() {
		return copySrcRevision;
	}

	public void setCopySrcRevision(long copySrcRevision) {
		this.copySrcRevision = copySrcRevision;
	}

	public String getCopySrcPath() {
		return copySrcPath;
	}

	public void setCopySrcPath(String copySrcPath) {
		this.copySrcPath = copySrcPath;
	}
	
	public String toString() {
		String pattern = "{0} by {1} {3} on {2} -- {4} --";
		return MessageFormat.format(pattern, 
				new Object[]{ new Long(revision), 
				author, path, action+"", message});
	}
	
}

package org.eclipse.team.svn.core.internal.history;

public class Tags {
	private Alias[] tags;
	
	public Tags() {
		super();
	}
	
	public Tags(Alias[] tags) {
		this();
		this.tags = tags;
	}

	public Alias[] getTags() {
		return tags;
	}

	public void setTags(Alias[] tags) {
		this.tags = tags;
	}

}
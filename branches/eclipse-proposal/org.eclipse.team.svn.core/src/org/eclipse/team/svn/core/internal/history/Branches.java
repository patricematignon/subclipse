package org.eclipse.team.svn.core.internal.history;

public class Branches {
	private Alias[] branches;
	
	public Branches() {
		super();
	}
	
	public Branches(Alias[] branches) {
		this();
		this.branches = branches;
	}

	public Alias[] getBranches() {
		return branches;
	}

	public void setBranches(Alias[] branches) {
		this.branches = branches;
	}

}

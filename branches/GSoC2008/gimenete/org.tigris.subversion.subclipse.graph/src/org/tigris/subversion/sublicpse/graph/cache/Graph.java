package org.tigris.subversion.sublicpse.graph.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph implements Serializable {
	
	private static final long serialVersionUID = -5285462558875510455L;
	
	private String rootPath;
	private Map branches = new HashMap();
	private Set paths = new HashSet();
	
	public Graph(String rootPath) {
		this.rootPath = rootPath;
	}
	
	public void addBranch(Branch b) {
		String path = b.getPath();
		branches.put(path, b);
		paths.add(path);
	}
	
	public Set getPaths() {
		return paths;
	}
	
	public Branch getBranch(String path) {
		return (Branch) branches.get(path);
	}
	
	public String getRootPath() {
		return rootPath;
	}
	
}

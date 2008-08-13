package org.tigris.subversion.sublicpse.graph.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph implements Serializable {

	private static final String[] EMPTY_STRING = {};
	private static final long serialVersionUID = -5285462558875510455L;
	
	private String rootPath;
	private Map branches = new HashMap();
	private List paths = new ArrayList();
	private transient String[] pathsArray = null;
	
	public Graph(String rootPath) {
		this.rootPath = rootPath;
	}
	
	public Branch addBranch(String path) {
		Branch b = new Branch(path);
		branches.put(path, b);
		paths.add(path);
		pathsArray = null;
		return b;
	}
	
	public List getPaths() {
		return paths;
	}
	
	public Branch getBranch(String path) {
		return (Branch) branches.get(path);
	}
	
	public String getRootPath() {
		return rootPath;
	}

	public String[] getPathsAsArray() {
		if(pathsArray == null)
			pathsArray = (String[]) paths.toArray(EMPTY_STRING);
		return pathsArray;
	}
	
}

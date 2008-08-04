package org.tigris.subversion.sublicpse.graph.cache;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;

public class Cache {

	private static final String[] EMPTY_STRING = {};
	private static final int MAX_LOG_MESSAGES = 1024;
	
	private File revisionsFile;
	private File logMessagesFile;

	private File root;

	// used in updates
	private RandomAccessFile revisionsRaf = null;
	private RandomAccessFile logMessagesRaf = null;
	
	public Cache(File f) {
		this.root = f;
		String databaseName = f.getAbsolutePath();
		if(File.pathSeparator.equals("\\"))
			databaseName = databaseName.replace('\\', '/');
		
		if(!f.exists()) {
			if(!f.mkdir()) {
				throw new CacheException("Couldn't create directory: "+f.getAbsolutePath());
			}
		}
		revisionsFile = new File(root, "revisions");
		logMessagesFile = new File(root, "logMessages");
	}
	
	public void close() {
		closeFile(revisionsRaf);
		closeFile(logMessagesRaf);
	}
	
	public void startUpdate() {
		try {
			revisionsRaf = new RandomAccessFile(revisionsFile, "rw");
			logMessagesRaf = new RandomAccessFile(logMessagesFile, "rw");

			revisionsRaf.seek(revisionsRaf.length());
			logMessagesRaf.seek(logMessagesRaf.length());
		} catch(IOException e) {
			throw new CacheException("Error while opening file", e);
		}
	}
	
	private String notNull(String s) {
		if(s == null)
			return "";
		return s;
	}
	
	private void writeLogMessage(ISVNLogMessage logMessage) throws IOException {
		long revision = logMessage.getRevision().getNumber();
		long fp = logMessagesRaf.getFilePointer();
//		System.out.println("writing rev "+revision+" at "+fp+" "+revisions.getFilePointer());
		revisionsRaf.writeLong(fp);

		logMessagesRaf.writeLong(revision);
		logMessagesRaf.writeLong(logMessage.getDate().getTime());
		logMessagesRaf.writeUTF(notNull(logMessage.getAuthor()));
		logMessagesRaf.writeUTF(notNull(logMessage.getMessage()));
		
		ISVNLogMessageChangePath[] changePaths = logMessage.getChangedPaths();
		logMessagesRaf.writeInt(changePaths.length);
		for (int i = 0; i < changePaths.length; i++) {
			ISVNLogMessageChangePath changePath = changePaths[i];

			logMessagesRaf.writeChar(changePath.getAction());
			logMessagesRaf.writeUTF(changePath.getPath());
			long copySrcRevision = 0;
			if(changePath.getCopySrcRevision() != null) {
				copySrcRevision = changePath.getCopySrcRevision().getNumber();
				logMessagesRaf.writeLong(copySrcRevision);
				logMessagesRaf.writeUTF(changePath.getCopySrcPath());
			} else {
				logMessagesRaf.writeLong(copySrcRevision);
			}
		}
	}
	
	public void finishUpdate() {
		closeFile(revisionsRaf);
		closeFile(logMessagesRaf);
		revisionsRaf = null;
		logMessagesRaf = null;
	}
	
	public void update(ISVNLogMessage logMessage) {
		try {
			writeLogMessage(logMessage);
		} catch (IOException e) {
			throw new CacheException("Error while saving log message", e);
		}
	}
	
	public long getLatestRevision() {
		return revisionsFile.length() / 8;
	}
	
	public void readNext(RandomAccessFile file, ISVNLogMessage[] buffer, int number) {
		do {
			number--;
			buffer[number] = readNext(file);
		} while(number > 0);
	}
	
	public Node findRootNode(String path, long revision, WorkListener listener) {
		long r = revision;
		long pr = r;
		RandomAccessFile logMessages = null;
		try {
			logMessages = new RandomAccessFile(logMessagesFile, "r");
			ISVNLogMessage[] buffer = new ISVNLogMessage[MAX_LOG_MESSAGES];
			do {
				r -= (MAX_LOG_MESSAGES)-1;
				if(r < 1) {
					r = 1;
				}
				logMessages.seek(getSeek(r));
				int size = (int) (pr - r + 1); // this won't be higher than MAX_LOG_MESSAGES
				if(size == 0) break;
				readNext(logMessages, buffer, size);
				
				for (int k = 0; k < size; k++) {
					ISVNLogMessage lm = buffer[k];
//					System.out.println("revision: "+lm.getRevision().getNumber());
					
					ISVNLogMessageChangePath[] changedPaths = lm.getChangedPaths();
					for(int n=0; n<changedPaths.length; n++) {
						ISVNLogMessageChangePath cp = changedPaths[n];

						if(lm.getRevision().getNumber() <= revision && cp.getAction() == 'A') {
							if(cp.getCopySrcPath() != null) {
								if(isEqualsOrParent(cp.getPath(), path)) {
									revision = lm.getRevision().getNumber();
									path = cp.getCopySrcPath() + path.substring(cp.getPath().length());
									// TODO: here I could seek to 'revision'
									// because all other revisions in between will be ignored
								}
							} else {
								if(cp.getPath().equals(path)) {
									revision = lm.getRevision().getNumber();

									Node node = new Node();
									node.setPath(path);
									node.setRevision(revision);
									return node;
								}
							}
						}
					}

					if(listener != null)
						listener.worked();
				}
				pr = r;
			} while(true);
		} catch(IOException e) {
			throw new CacheException("Error while finding root node", e);
		} finally {
			closeFile(logMessages);
		}
		
		Node n = new Node();
		n.setPath(path);
		n.setRevision(revision);
		return n;
	}
	
	private ISVNLogMessage readNext(RandomAccessFile file) {
		try {
			long revision = file.readLong();
//			System.out.println("reading revision: "+revision+" "+(file.getFilePointer()-8));
			long date = file.readLong();
			String author = file.readUTF();
			String message = file.readUTF();
			LogMessage logMessage = new LogMessage(revision, author, new Date(date), message);
			
			int length = file.readInt();
			LogMessageChangePath[] changedPaths = new LogMessageChangePath[length];
			logMessage.setChangedPaths(changedPaths);
			for(int i=0; i<length; i++) {
				char action = file.readChar();
				String path = file.readUTF();
				long copySrcRevision = file.readLong();
				String copySrcPath = null;
				if(copySrcRevision > 0) {
					copySrcPath = file.readUTF();
				}
				
				changedPaths[i] = new LogMessageChangePath(action, path, copySrcPath, copySrcRevision);
			}
			
			return logMessage;
		} catch (IOException e) {
			throw new CacheException("Error while reading log messages from file", e);
		}
	}
	
	public long getSeek(long revision) {
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(revisionsFile, "r");
			file.seek((revision-1)*8);
			return file.readLong();
		} catch (IOException e) {
			throw new CacheException("Error while reading revisions file", e);
		} finally {
			closeFile(file);
		}
	}
	
	public Graph createGraph(String rootPath, long revision, WorkListener listener) {
//		System.out.println("create graph");
		Graph graph = new Graph(rootPath);
		// root path is the first opened branch
		graph.getPaths().add(rootPath);
		Branch rootBranch = new Branch(rootPath);
		graph.addBranch(rootBranch);
		
		long seek = getSeek(revision);
		
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(logMessagesFile, "r");
			file.seek(seek);

			while(file.getFilePointer() < file.length()) {
				ISVNLogMessage lm = readNext(file);

				ISVNLogMessageChangePath[] changedPaths = lm.getChangedPaths();
				for(int n=0; n<changedPaths.length; n++) {
					ISVNLogMessageChangePath cp = changedPaths[n];

					String nodePath = cp.getPath();
					String copySrcPath = cp.getCopySrcPath();

					String[] pa = (String[]) graph.getPaths().toArray(EMPTY_STRING);
					for (int i = 0; i < pa.length; i++) {
						String branchPath = pa[i];

						//					System.out.println();
						//					System.out.println(node.getRevision());
						//					System.out.println("cpsrc: "+copySrcPath);
						//					System.out.println("nodep: "+nodePath);
						//					System.out.println("brnch: "+branchPath);

						if(copySrcPath == null && isEqualsOrParent(nodePath, branchPath)) {
							Branch branch = graph.getBranch(branchPath);
							if(branch.isEnded()) {
								// the branch was ended with a D action
								continue;
							}
							Node node = toNode(lm, cp);
							node.setParent(branch.getLastNode());
							branch.addNode(node);
							if(node.getAction() == 'D') {
								branch.end();
							}
						} else if(copySrcPath != null && isEqualsOrParent(copySrcPath, branchPath)) {
							Branch branch = graph.getBranch(branchPath);
							Node source = branch.getSource(cp.getCopySrcRevision().getNumber());
							if(source == null)
								continue;
							Node node = toNode(lm, cp);
							node.setParent(source); // should it use a different method such as setSource ?
							String path = nodePath + branchPath.substring(copySrcPath.length());
							Branch newBranch = graph.getBranch(path);
							if(newBranch == null) {
								newBranch = new Branch(path);
								graph.addBranch(newBranch);
								graph.getPaths().add(path);
							}
							newBranch.addNode(node);
						}
					}
				}
				if(listener != null)
					listener.worked();
			}
		} catch(IOException e) {
			throw new CacheException("Error while calculating graph", e);
		} finally {
			closeFile(file);
		}
		return graph;
	}
	
	public Node toNode(ISVNLogMessage lm, ISVNLogMessageChangePath cp) {
		Node node = new Node();
		node.setAction(cp.getAction());
		node.setAuthor(lm.getAuthor());
		node.setCopySrcPath(cp.getCopySrcPath());
		node.setCopySrcRevision(cp.getCopySrcRevision().getNumber());
		node.setMessage(lm.getMessage());
		node.setPath(cp.getPath());
		node.setRevision(lm.getRevision().getNumber());
		node.setRevisionDate(lm.getDate());
		return node;
	}
	
//	public static void main(String[] args) {
//		System.out.println(isEqualsOrParent("/foo/bar/hello.java", "/foo/bar/hello.java")); // true
//		System.out.println(isEqualsOrParent("/foo/bar", "/foo/bar/hello.java")); // true
//		System.out.println(isEqualsOrParent("/foo/b", "/foo/bar/hello.java")); // false
//		System.out.println(isEqualsOrParent("/foo", "/foo/bar/hello.java")); // true
//		System.out.println(isEqualsOrParent("/fo", "/foo/bar/hello.java")); // false
//		System.out.println(isEqualsOrParent("/f", "/foo/bar/hello.java")); // false
//	}
	
	public static boolean isEqualsOrParent(String parent, String path) {
		return path.equals(parent) || path.startsWith(parent+"/"); // TODO: optimize
	}
	
	public Node mapRow(ResultSet resultSet) throws SQLException {
		Node node = new Node();
		node.setRevision(resultSet.getLong(1));
		node.setAuthor(resultSet.getString(2));
		node.setRevisionDate(resultSet.getTimestamp(3));
		node.setMessage(resultSet.getString(4));
		node.setPath(resultSet.getString(5));
		node.setAction(resultSet.getString(6).charAt(0));
		node.setCopySrcRevision(resultSet.getLong(7));
		node.setCopySrcPath(resultSet.getString(8));
		return node;
	}
	
	public void clearCache() {
		deleteFile(revisionsFile);
		deleteFile(logMessagesFile);
	}
	
	private void deleteFile(File f) {
		if(f.exists() && !f.delete()) {
			System.err.println("Couldn't delete file: "+f.getAbsolutePath());
		}
	}
	
	private void closeFile(RandomAccessFile file) {
		if(file == null) return;
		try {
			file.close();
		} catch (IOException e) {
			throw new CacheException("Error while closing file", e);
		}
	}

}

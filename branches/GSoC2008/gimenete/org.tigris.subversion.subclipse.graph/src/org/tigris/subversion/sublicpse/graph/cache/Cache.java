package org.tigris.subversion.sublicpse.graph.cache;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;

public class Cache {
	
	private static final String[] EMPTY_STRING = {};

	private static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
	
	private Connection connection;

//	private PreparedStatement insertRevision;
//	private PreparedStatement insertFile;
//	private PreparedStatement insertCopyFile;
//	private PreparedStatement insertChangePath;
//	private PreparedStatement deleteFile;
//	private PreparedStatement selectFiles;
	
	private Statement batch;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
	
	public Cache(File f) {
		try {
			String databaseName = f.getAbsolutePath();
			if(File.pathSeparator.equals("\\"))
				databaseName = databaseName.replace('\\', '/');

			Class.forName(DRIVER_CLASS_NAME);
			try {
				connection = 
					DriverManager.getConnection("jdbc:derby:"+databaseName);
			} catch(SQLException e) {
				// The database may not exist.
				connection = 
					DriverManager.getConnection("jdbc:derby:"+databaseName+";create=true");
				// Create tables
				createTablesAndIndexes();
			}
		} catch(Exception e) {
			throw new CacheException("Error creating cache", e);
		}
	}
	
	private void createTablesAndIndexes() throws Exception {
//		String s = "CREATE TABLE files (" +
//				"file_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
//				"revision_from BIGINT NOT NULL, " +
//				"revision_to BIGINT, " +
//				"path VARCHAR(32672) NOT NULL)";
//		
//		executeUpdate(s);

		String s = "CREATE TABLE revisions (" +
				"revision_id BIGINT NOT NULL, " +
				"revision_date TIMESTAMP NOT NULL, " +
				"author VARCHAR(32672) NOT NULL, " +
				"message VARCHAR(32672) NOT NULL, " +
				"PRIMARY KEY (revision_id))";

		executeUpdate(s);

		s = "CREATE TABLE change_paths (" +
				"path VARCHAR(32672) NOT NULL, " +
				"revision_id BIGINT NOT NULL, " +
				"copy_src_revision BIGINT, " +
				"copy_src_path VARCHAR(32672), " +
				"action CHAR(1) NOT NULL,"+
				"id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
				"PRIMARY KEY (id))";
//				"file_id BIGINT NOT NULL)";
//				"PRIMARY KEY (revision_id, path, action))"; // We can have an A and M change_path from the same path in the same revision
		
		executeUpdate(s);

//		s = "ALTER TABLE change_paths ADD CONSTRAINT fk_change_revision " +
//				"FOREIGN KEY (revision_id) REFERENCES revisions (revision_id)";
//		
//		executeUpdate(s);
//
//		s = "CREATE INDEX files_revision_from ON files (revision_from)";
//		
//		executeUpdate(s);
//
//		s = "CREATE INDEX files_revision_to ON files (revision_to)";
//		
//		executeUpdate(s);
//
//		s = "CREATE INDEX files_path ON files (path)";
//		
//		executeUpdate(s);
//
//		s = "CREATE INDEX files_file_id ON files (file_id)";
//		
//		executeUpdate(s);
		
	}
	
	public void close() {
//		closeStatement(insertRevision);
//		closeStatement(insertFile);
//		closeStatement(insertCopyFile);
//		closeStatement(insertChangePath);
//		closeStatement(deleteFile);
//		closeStatement(selectFiles);
		
		try {
			connection.close();
		} catch (SQLException e) {
			throw new CacheException("Error closing database connection", e);
		}
	}
	
	private void closeStatement(Statement statement) {
		if(statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				throw new CacheException("Error closing statement", e);
			}
		}
	}
	
	private void closeResultSet(ResultSet result) {
		if(result != null) {
			try {
				result.close();
			} catch (SQLException e) {
				throw new CacheException("Error closing ResultSet", e);
			}
		}
	}
	
	private String escape(String s) {
		if(s == null)
			return "NULL";
		s = s.replace("\'", "''");
		s = s.replace("\t", "\\t");
		s = s.replace("\r", "\\r");
		s = s.replace("\n", "\\n");
//		s = s.replace("\"", "\\\"");
		return "'"+s+"'";
	}
	
	public void startUpdate() {
		try {
			batch = connection.createStatement();
		} catch (SQLException e) {
			throw new CacheException("Error when starting batch update", e);
		}
	}
	
	public void executeUpdate() {
		try {
			batch.executeBatch();
		} catch (SQLException e) {
			throw new CacheException("Error while executing batch update", e);
		}
	}

	private void insertRevision(ISVNLogMessage logMessage) {
		String author = escape(logMessage.getAuthor());
		String message = escape(logMessage.getMessage());
		String s = "INSERT INTO revisions (" +
						"revision_id, " +
						"revision_date, " +
						"author, " +
						"message" +
						") VALUES ("+logMessage.getRevision()+", '"+dateFormat.format(logMessage.getDate())+"', "+author+", "+message+")";
		try {
			batch.addBatch(s);
		} catch (SQLException e) {
			throw new CacheException("Error while inserting revision", e);
		}
	}
	
	/*
	private long insertFile(long revFrom, String path) {
		if(insertFile == null) {
			try {
				insertFile = connection.prepareStatement("INSERT INTO files (" +
					"revision_from, " +
					"path" +
					") VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
			} catch(SQLException e) {
				throw new CacheException("Cannot create insertFile statement", e);
			}
		}
		
		ResultSet result = null;
		try {
			insertFile.setLong(1, revFrom);
			insertFile.setString(2, path);
			insertFile.executeUpdate();
			
			result = insertFile.getGeneratedKeys();
			if(result != null && result.next()) {
				return result.getLong(1);
			} else {
				throw new CacheException("Not generated key for inserted file");
			}
		} catch(SQLException e) {	
			throw new CacheException("Error inserting revision", e);
		} finally {
			closeResultSet(result);
		}
	}
	
	private void insertCopyFile(long fileId, long revision, String path) {
		if(insertCopyFile == null) {
			try {
				insertCopyFile = connection.prepareStatement("INSERT INTO files (" +
					"file_id, "+
					"revision_from, " +
					"path" +
					") VALUES (?, ?, ?)",
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			} catch(SQLException e) {
				throw new CacheException("Cannot create insertCopyFile statement", e);
			}
		}
		
		try {
			insertCopyFile.setLong(1, fileId);
			insertCopyFile.setLong(2, revision);
			insertCopyFile.setString(3, path);
			insertCopyFile.executeUpdate();
		} catch(SQLException e) {
			throw new CacheException("Error inserting file branched", e);
		}
	}
	*/
	
	private void insertChangePath(long revision, ISVNLogMessageChangePath changePath) {
		String path = escape(changePath.getPath());
		String copySrcPath = escape(changePath.getCopySrcPath());
		String action = escape(changePath.getAction()+"");
		String copySrcRevision = changePath.getCopySrcRevision() == null ? "NULL" :
				Long.toString(changePath.getCopySrcRevision().getNumber());
		String s = "INSERT INTO change_paths (" +
						"revision_id, " +
						"copy_src_revision, " +
						"copy_src_path, " +
						"action, " +
						"path" +
						") VALUES ("+revision+", "+copySrcRevision+", "+copySrcPath+", "+action+", "+path+")";
//		System.out.println(s);
		try {
			batch.addBatch(s);
		} catch (SQLException e) {
			throw new CacheException("Error while inserting change path", e);
		}
	}
	
	public void update(ISVNLogMessage logMessage) {
		long revision = logMessage.getRevision().getNumber();
		insertRevision(logMessage);
		
		ISVNLogMessageChangePath[] changedPaths = logMessage.getChangedPaths();
		for (int j = 0; j < changedPaths.length; j++) {
			insertChangePath(revision, changedPaths[j]);
		}
	}
	
	/*
	public Long getFileId(String path, long revision) {
		String sql = "SELECT file_id FROM files " +
			"WHERE path=? AND revision_from<=? AND (revision_to>? OR revision_to IS NULL)"; // revision_to>=? 

		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = connection.prepareStatement(sql);
			statement.setString(1, path);
			statement.setLong(2, revision);
			statement.setLong(3, revision);
			result = statement.executeQuery();
			if(result.next()) {
				return new Long(result.getLong(1));
			}
			return null;
		} catch(SQLException e) {
			throw new CacheException(e);
		} finally {
			closeResultSet(result);
			closeStatement(statement);
		}
	}
	*/
	
	public long getLatestRevision() {
		String sql = "SELECT MAX(revision_id) from revisions";

		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = connection.prepareStatement(sql);
			result = statement.executeQuery();
			if(result.next()) {
				return result.getLong(1);
			}
			return 0L;
		} catch(SQLException e) {
			throw new CacheException(e);
		} finally {
			closeResultSet(result);
			closeStatement(statement);
		}
	}
	
	public Node findRootNode(String path, long revision, WorkListener listener) {
		String sql = "SELECT " +
			"revisions.revision_id, " +
			"revisions.author, " +
			"revisions.revision_date, " +
			"revisions.message," +
			"change_paths.path, " +
			"change_paths.action, " +
			"change_paths.copy_src_revision, " +
			"change_paths.copy_src_path " +
			"FROM change_paths, revisions " +
			"WHERE change_paths.revision_id=revisions.revision_id AND revisions.revision_id<="+ revision +
			" ORDER BY change_paths.id DESC";
		ResultSet resultSet = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			
			long pr = revision; // previous revision
			
			while(resultSet.next()) {
				Node node = mapRow(resultSet);

				if(listener != null && pr != node.getRevision()) {
					listener.worked();
					pr = node.getRevision();
				}
				
				if(node.getRevision() <= revision && node.getAction() == 'A') {
					if(node.getCopySrcPath() != null) {
						if(isEqualsOrParent(node.getPath(), path)) {
							revision = node.getRevision();
							path = node.getCopySrcPath() + path.substring(node.getPath().length());
						}
					} else {
						if(node.getPath().equals(path)) {
							revision = node.getRevision();
							break;
						}
					}
				}
			}
			
			Node node = new Node();
			node.setPath(path);
			node.setRevision(revision);
			return node;
		} catch(SQLException e) {
			throw new CacheException("Error finding root node", e);
		} finally {
			closeResultSet(resultSet);
			closeStatement(statement);
		}
	}
	
	public Graph createGraph(String rootPath, long revision, WorkListener listener) {
		Graph graph = new Graph(rootPath);
		String sql = "SELECT " +
			"revisions.revision_id, " +
			"revisions.author, " +
			"revisions.revision_date, " +
			"revisions.message," +
			"change_paths.path, " +
			"change_paths.action, " +
			"change_paths.copy_src_revision, " +
			"change_paths.copy_src_path " +
			"FROM change_paths, revisions " +
			"WHERE change_paths.revision_id=revisions.revision_id AND revisions.revision_id>="+ revision +
			" ORDER BY change_paths.id ASC";
		ResultSet resultSet = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			Set paths = graph.getPaths();
			// root path is the first opened branch
			paths.add(rootPath);
			Branch rootBranch = new Branch(rootPath);
			graph.addBranch(rootBranch);
			
			long pr = revision; // previous revision
			
			while(resultSet.next()) {
				Node node = mapRow(resultSet);

				if(listener != null && pr != node.getRevision()) {
					listener.worked();
					pr = node.getRevision();
				}
				
				String nodePath = node.getPath();
				String copySrcPath = node.getCopySrcPath();
				
				String[] pa = (String[]) paths.toArray(EMPTY_STRING);
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
							// the branch has ended with a D action
							continue;
						}
						node.setParent(branch.getLastNode());
						branch.addNode(node);
						if(node.getAction() == 'D') {
							branch.end();
						}
					} else if(copySrcPath != null && isEqualsOrParent(copySrcPath, branchPath)) {
						Branch branch = graph.getBranch(branchPath);
						Node source = branch.getSource(node.getCopySrcRevision());
						if(source == null)
							continue;
						node.setParent(source); // should it use a different method such as setSource ?
						String path = nodePath + branchPath.substring(copySrcPath.length());
						Branch newBranch = graph.getBranch(path);
						if(newBranch == null) {
							newBranch = new Branch(path);
							graph.addBranch(newBranch);
							paths.add(path);
						}
						newBranch.addNode(node);
					}
				}
			}
		} catch (SQLException e) {
			throw new CacheException("Error while calculating graph", e);
		} finally {
			closeStatement(statement);
			closeResultSet(resultSet);
		}
		
		return graph;
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
	
	private void executeUpdate(String sql) {
		try {
			connection.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			throw new CacheException("Error creating database structure", e);
		}
	}
	
	/*
	public void dumpChangePaths() {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("SELECT revision_id, copy_src_revision, copy_src_path, action, path FROM change_paths");
			while(r.next()) {
				String str = MessageFormat.format("{0}, {3}, {4}, {1}, {2}", new Object[]{
						r.getObject(1),
						r.getObject(2),
						r.getObject(3),
						r.getObject(4),
						r.getObject(5)
				});
				System.out.println(str);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dumpRevisions() {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("SELECT revision_id, " +
						"revision_date, " +
						"author, " +
						"message FROM revisions");
			while(r.next()) {
				String str = MessageFormat.format("{0}, {1}, {2}, {3}", new Object[]{
						r.getObject(1),
						r.getObject(2),
						r.getObject(3),
						r.getObject(4)
				});
				System.out.println(str);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
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
		executeUpdate("DELETE FROM change_paths");
		executeUpdate("DELETE FROM revisions");
//		executeUpdate("DELETE FROM files");
	}

}

package org.universAAL.ri.api.manager.server.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.universAAL.ri.api.manager.Activator;
import org.universAAL.ri.api.manager.RemoteAPI;

/**
 * An implementation of the Persistence interface using Apache Derby SQL
 * Database.
 * 
 * @author alfiva
 * 
 */
public class PersistenceDerby implements Persistence {

    /**
     * Name of the root Database
     */
    private static final String DBNAME = "RAPIDB";
    /**
     * Instance of the Remote API being used in the manager
     */
    private RemoteAPI api;
    /**
     * Full URL of the Database
     */
    private String dbURL;

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#init(org.universAAL.ri.api.manager.RemoteAPI)
     */
    public void init(RemoteAPI remoteAPI) {
	this.api = remoteAPI;
	dbURL = "jdbc:derby:/RAPIPersistence;create=true";
	Connection conn = null;
	Statement stmt = null;
	try {
	    new org.apache.derby.jdbc.EmbeddedDriver();
	    
	    conn = DriverManager.getConnection(dbURL);
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String createREGISTERS = "CREATE TABLE " + DBNAME
		    + ".registers ( id varchar(512) PRIMARY KEY NOT NULL, remote  varchar(512)," +
		    " tstmp timestamp )";
	    String createSUBSCRIBERS = "CREATE TABLE " + DBNAME
		    + ".subscribers ( id varchar(512) PRIMARY KEY NOT NULL, pattern varchar(5120)," +
		    " tstmp timestamp, CONSTRAINT subid_fk FOREIGN KEY (id) REFERENCES "
		    + DBNAME + ".registers(id))";
	    String createCALLEES = "CREATE TABLE " + DBNAME
		    + ".callees ( id varchar(512) PRIMARY KEY NOT NULL, profile varchar(5120)," +
		    " tstmp timestamp, CONSTRAINT calid_fk FOREIGN KEY (id)  REFERENCES "
		    + DBNAME + ".registers(id))";

	    // create tables. Put individual try/catch to ignore already created. I dont trust addBatch.
	    try {
		stmt.executeUpdate(createREGISTERS);
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    Activator.logI("PersistenceDerby.init", "Database already exists");
		}
	    }
	    try {
		stmt.executeUpdate(createSUBSCRIBERS);
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    Activator.logI("PersistenceDerby.init", "Database already exists");
		}
	    }
	    try {
		stmt.executeUpdate(createCALLEES);
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    Activator.logI("PersistenceDerby.init", "Database already exists");
		}
	    }

	} catch (Exception e) {
	    Activator.logE("PersistenceDerby.init", "Error creating database");
	    e.printStackTrace();
	} finally {
	    try {
		if (stmt != null) {
		    stmt.close();
		}
		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException e) {
		Activator.logE("PersistenceDerby.init", "Error creating database");
		e.printStackTrace();
	    }
	}
	
	removeOlderThan(-1l);// -1 : Disabled
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#storeRegister(java.lang.String, java.lang.String)
     */
    public void storeRegister(String id, String remote) {
	Timestamp t = new Timestamp(System.currentTimeMillis());
	String storeREGISTERS = "insert into " + DBNAME
		+ ".registers (id, remote, tstmp) values ('" + id + "','"
		+ remote + "','" + t.toString() + "')";
	executeGeneric(storeREGISTERS);
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#storeSubscriber(java.lang.String, java.lang.String)
     */
    public void storeSubscriber(String id, String pattern) {
	Timestamp t = new Timestamp(System.currentTimeMillis());
	String storeREGISTERS = "insert into " + DBNAME
		+ ".subscribers (id, pattern, tstmp) values ('" + id + "','"
		+ pattern + "','" + t.toString() + "')";
	executeGeneric(storeREGISTERS);
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#storeCallee(java.lang.String, java.lang.String)
     */
    public void storeCallee(String id, String profile) {
	Timestamp t = new Timestamp(System.currentTimeMillis());
	String storeREGISTERS = "insert into " + DBNAME
		+ ".callees (id, profile, tstmp) values ('" + id + "','"
		+ profile + "','" + t.toString() + "')";
	executeGeneric(storeREGISTERS);
    }
    
    /**
     * Convenient method to execute storeXXX methods of the interface, through a
     * SQL INSERT (though it could be any SQL sentence that requires using
     * .execute)
     * 
     * @param sql
     *            The SQL instruction to execute
     */
    private void executeGeneric(String sql){
	Connection conn = null;
	Statement stmt = null;
	try {
	    new org.apache.derby.jdbc.EmbeddedDriver();
	    
	    conn = DriverManager.getConnection(dbURL);
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);
	    stmt.executeUpdate(sql);

	} catch (Exception e) {
	    Activator.logE("PersistenceDerby.executeGeneric", "Error writing in the database");
	    e.printStackTrace();
	} finally {
	    try {
		if (stmt != null) {
		    stmt.close();
		}
		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException e) {
		Activator.logE("PersistenceDerby.executeGeneric", "Error writing in the database");
		e.printStackTrace();
	    }
	}
    }
    
    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#removeRegister(java.lang.String)
     */
    public void removeRegister(String id) {
	Connection conn = null;
	Statement stmt = null;
	try {
	    new org.apache.derby.jdbc.EmbeddedDriver();

	    conn = DriverManager.getConnection(dbURL);
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String deleteCALLEES = "delete from " + DBNAME + ".callees WHERE id='" + id + "'";
	    String deleteSUBSCRIBERS = "delete from " + DBNAME + ".subscribers WHERE id='" + id + "'";
	    String deleteREGISTERS = "delete from " + DBNAME + ".registers WHERE id='" + id + "'";
	    // create tables. Again, put individual try/catch to keep deleting if errors. I dont trust addBatch.
	    try {
		stmt.executeUpdate(deleteCALLEES);
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    e.printStackTrace();
		}
	    }
	    try {
		stmt.executeUpdate(deleteSUBSCRIBERS);
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    e.printStackTrace();
		}
	    }
	    try {
		stmt.executeUpdate(deleteREGISTERS);// TODO Remove in cascade
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    e.printStackTrace();
		}
	    }

	} catch (Exception e) {
	    Activator.logE("PersistenceDerby.removeRegister", "Error removing from the database");
	    e.printStackTrace();
	} finally {
	    try {
		if (stmt != null) {
		    stmt.close();
		}
		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException e) {
		Activator.logE("PersistenceDerby.removeRegister", "Error removing from the database");
		e.printStackTrace();
	    }
	}
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#restore()
     */
    public void restore() {
	Connection conn = null;
	Statement stmt = null;
	try {
	    new org.apache.derby.jdbc.EmbeddedDriver();
	    
	    conn = DriverManager.getConnection(dbURL);
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String selectREGISTERS="Select * from "+DBNAME+".registers";
	    String selectSUSBCRIBERS="Select * from "+DBNAME+".subscribers";
	    String selectCALLEES="Select * from "+DBNAME+".callees";
	    
	    ResultSet resultSet = stmt.executeQuery(selectREGISTERS); 
	    while (resultSet.next()){
		String id=resultSet.getString(1);
		String remote=resultSet.getString(2);
		if(id!=null && remote!=null){
		    this.api.register(id, remote);
		}
	    }
	    // No need to link from id to id between tables,
	    // just iterate once everything is registered
	    resultSet = stmt.executeQuery(selectSUSBCRIBERS);
	    while (resultSet.next()){
		String id=resultSet.getString(1);
		String pattern=resultSet.getString(2);
		if(id!=null && pattern!=null){
		    this.api.subscribeC(id, pattern);
		}
	    }
	    
	    resultSet = stmt.executeQuery(selectCALLEES);
	    while (resultSet.next()){
		String id=resultSet.getString(1);
		String profile=resultSet.getString(2);
		if(id!=null && profile!=null){
		    this.api.provideS(id, profile);
		}
	    }

	} catch (Exception e) {
	    Activator.logE("PersistenceDerby.restore", "Error restoring the database");
	    e.printStackTrace();
	} finally {
	    try {//javadoc: resultSet auto closed if stmt is closed
		if (stmt != null) {
		    stmt.close();
		}
		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException e) {
		Activator.logE("PersistenceDerby.restore", "Error restoring the database");
		e.printStackTrace();
	    }
	}
    }

    /**
     * Maintenance method that removes all stored data from all tables that was
     * introduced more than @param millis ago. Removal is not accurate to the
     * millisecond, it rounds to the day.
     * 
     * @param millis
     *            Minimum age, in milliseconds, at which the data that will be
     *            removed (data older than this will be removed)
     */
    public void removeOlderThan(Long millis) {
	if (millis<0l)return;//Disable with millis <0
	Timestamp t=new Timestamp(System.currentTimeMillis()-millis);//Date up to which remove
	Connection conn = null;
	Statement stmt = null;
	try {
	    new org.apache.derby.jdbc.EmbeddedDriver();

	    conn = DriverManager.getConnection(dbURL);
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    // Remove where t - tstmp > 0 (that is, the date -in days- of the
	    // row is older than the date up to which remove)
	    String deleteCALLEES = "delete from " + DBNAME
		    + ".callees WHERE {fn TIMESTAMPDIFF( SQL_TSI_DAY, tstmp, '"
		    + t.toString() + "')} > 0";
	    String deleteSUBSCRIBERS = "delete from "
		    + DBNAME
		    + ".subscribers WHERE {fn TIMESTAMPDIFF( SQL_TSI_DAY, tstmp, '"
		    + t.toString() + "')} > 0";
	    String deleteREGISTERS = "delete from "
		    + DBNAME
		    + ".registers WHERE {fn TIMESTAMPDIFF( SQL_TSI_DAY, tstmp, '"
		    + t.toString() + "')} > 0";
	    // create tables. Again, put individual try/catch to keep deleting
	    // if errors. I dont trust addBatch.
	    try {
		stmt.executeUpdate(deleteCALLEES);
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    e.printStackTrace();
		}
	    }
	    try {
		stmt.executeUpdate(deleteSUBSCRIBERS);
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    e.printStackTrace();
		}
	    }
	    try {
		stmt.executeUpdate(deleteREGISTERS);// TODO Remove in cascade
	    } catch (SQLException e) {
		if (!e.getSQLState().equals("X0Y32")) {
		    e.printStackTrace();
		}
	    }

	} catch (Exception e) {
	    Activator.logE("PersistenceDerby.removeOlderThan", "Error cleaning the database");
	    e.printStackTrace();
	} finally {
	    try {
		if (stmt != null) {
		    stmt.close();
		}
		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException e) {
		Activator.logE("PersistenceDerby.removeOlderThan", "Error cleaning the database");
		e.printStackTrace();
	    }
	}
    }

}

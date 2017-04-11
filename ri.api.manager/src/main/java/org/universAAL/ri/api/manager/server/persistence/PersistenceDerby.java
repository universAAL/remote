/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.ri.api.manager.server.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.ri.api.manager.Activator;
import org.universAAL.ri.api.manager.Configuration;
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
     * Name of the pwd Database, separate from the rest
     */
    private static final String PWDDBNAME = "PWDRAPIDB";
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
    public void init(RemoteAPI remoteAPI, ModuleContext context) {
	this.api = remoteAPI;
	dbURL = "jdbc:derby:" + Configuration.getDBPath(context);
	String dbUSR=Configuration.getDBUser();
	String dbPWD=Configuration.getDBPass();
	if(dbUSR!=null && dbPWD!=null){
	    dbURL+=";user="+dbUSR+";password="+dbPWD;
	}
	String bootURL=dbURL+";create=true;dataEncryption=true;bootPassword="
		+ Configuration.getDBKey();
	Connection conn = null;
	Statement stmt = null;
	try {
	    new org.apache.derby.jdbc.EmbeddedDriver();
	    
	    conn = DriverManager.getConnection(bootURL);
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String createREGISTERS = "CREATE TABLE " + DBNAME
		    + ".registers ( id varchar(512) PRIMARY KEY NOT NULL, remote  varchar(512)," +
		    " tstmp timestamp, version varchar(25) )";
	    String createSUBSCRIBERS = "CREATE TABLE " + DBNAME
		    + ".subscribers (rowid integer PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY " +
		    "(START WITH 1, INCREMENT BY 1), id varchar(512) NOT NULL, pattern varchar(5120)," +
		    " tstmp timestamp, CONSTRAINT subid_fk FOREIGN KEY (id) REFERENCES "
		    + DBNAME + ".registers(id))";
	    String createCALLEES = "CREATE TABLE " + DBNAME
		    + ".callees (rowid integer PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY " +
		    "(START WITH 1, INCREMENT BY 1), id varchar(512) NOT NULL, profile varchar(5120)," +
		    " tstmp timestamp, CONSTRAINT calid_fk FOREIGN KEY (id)  REFERENCES "
		    + DBNAME + ".registers(id))";
	    String createPWDS = "CREATE TABLE " + PWDDBNAME
		    + ".pwds ( id varchar(512) PRIMARY KEY NOT NULL, pwd  varchar(100) )";

	    // create tables. Put individual try/catch to ignore already created. I dont trust addBatch.
	    try {
		stmt.executeUpdate(createREGISTERS);
	    } catch (SQLException e) {
		if (e.getSQLState().equals("X0Y32")) {
		    Activator.logI("PersistenceDerby.init", "Database already exists");
		}else{
		    Activator.logE("PersistenceDerby.init", "Error creating database, REGISTERS");
		    e.printStackTrace();
		}
	    }
	    try {
		stmt.executeUpdate(createSUBSCRIBERS);
	    } catch (SQLException e) {
		if (e.getSQLState().equals("X0Y32")) {
		    Activator.logI("PersistenceDerby.init", "Database already exists");
		}else{
		    Activator.logE("PersistenceDerby.init", "Error creating database, SUBSCRIBERS");
		    e.printStackTrace();
		}
	    }
	    try {
		stmt.executeUpdate(createCALLEES);
	    } catch (SQLException e) {
		if (e.getSQLState().equals("X0Y32")) {
		    Activator.logI("PersistenceDerby.init", "Database already exists");
		}else{
		    Activator.logE("PersistenceDerby.init", "Error creating database, CALLEES");
		    e.printStackTrace();
		}
	    }
	    try {
		stmt.executeUpdate(createPWDS);
	    } catch (SQLException e) {
		if (e.getSQLState().equals("X0Y32")) {
		    Activator.logI("PersistenceDerby.init", "Database already exists");
		}else{
		    Activator.logE("PersistenceDerby.init", "Error creating database, PWDS");
		    e.printStackTrace();
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
	
	removeOlderThan(Configuration.getRemovalTime());// -1 : Disabled
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#storeRegister(java.lang.String, java.lang.String)
     */
    public void storeRegister(String id, String remote, String v) {
	Timestamp t = new Timestamp(System.currentTimeMillis());
	String storeREGISTERS;
	if (checkUserFromREGISTERS(id)) {
	    // This id already registered -> update remote and t and v
	    storeREGISTERS = "update " + DBNAME + ".registers SET remote='"
		    + remote + "', tstmp='" + t +"'"+ (v!=null?", version='"+v+"'":"") +" WHERE id='" + id + "'";
	}else{
	    //New id - > insert all
	    storeREGISTERS = "insert into " + DBNAME
		    + ".registers (id, remote, tstmp, version) values ('" + id + "','"
		    + remote + "','" + t.toString() + "', " + (v!=null?"'"+v+"'":"NULL") +")";
	}
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
		stmt.executeUpdate(deleteREGISTERS);// TODO Remove in cascade? Rsult is the same, but maybe faster?
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
		String id=resultSet.getString("id");
		String remote=resultSet.getString("remote");
		if(id!=null && remote!=null){
		    this.api.register(id, remote);
		}
	    }
	    // No need to link from id to id between tables,
	    // just iterate once everything is registered
	    resultSet = stmt.executeQuery(selectSUSBCRIBERS);
	    while (resultSet.next()){
		String id=resultSet.getString("id");
		String pattern=resultSet.getString("pattern");
		if(id!=null && pattern!=null){
		    this.api.subscribeC(id, pattern);
		}
	    }
	    
	    resultSet = stmt.executeQuery(selectCALLEES);
	    while (resultSet.next()){
		String id=resultSet.getString("id");
		String profile=resultSet.getString("profile");
		if(id!=null && profile!=null){
		    this.api.provideS(id, profile);
		}
	    }

	} catch (Exception e) {
	    Activator.logE("PersistenceDerby.restore", "Error restoring from the database");
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
		Activator.logE("PersistenceDerby.restore", "Error restoring from the database");
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
     *            Minimum age, in milliseconds, at which the data will be
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
		stmt.executeUpdate(deleteREGISTERS);// TODO Remove in cascade? Rsult is the same, but maybe faster?
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

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#storeUserPWD(java.lang.String, java.lang.String)
     */
    public void storeUserPWD(String id, String pwd) {
	String storeREGISTERS = "insert into " + PWDDBNAME
		+ ".pwds (id, pwd) values ('" + id + "','"
		+ pwd + "')";
	executeGeneric(storeREGISTERS);
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#checkUserPWD(java.lang.String, java.lang.String)
     */
    public boolean checkUserPWD(String id, String pwd) {
	Connection conn = null;
	Statement stmt = null;
	boolean result=false;
	try {
	    new org.apache.derby.jdbc.EmbeddedDriver();
	    
	    conn = DriverManager.getConnection(dbURL);
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String selectPWDS="Select pwd from "+PWDDBNAME+".pwds WHERE id='"+id+"'";
	    
	    ResultSet resultSet = stmt.executeQuery(selectPWDS); 
	    while (resultSet.next()){
		String storedpwd=resultSet.getString(1);
		if(storedpwd.equals(pwd)){
		    result=true;
		}
	    }

	} catch (Exception e) {
	    Activator.logE("PersistenceDerby.checkUserPWD", "Error restoring from the database");
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
		Activator.logE("PersistenceDerby.checkUserPWD", "Error restoring from the database");
		e.printStackTrace();
	    }
	}
	return result;
    }
    
    /**
     * Check if there is already a row with a certain id in the .registers
     * table.
     * 
     * @param id
     *            The id column value to look for
     * @return true if it already exists
     */
    private boolean checkUserFromREGISTERS(String id){
	return checkUserFromDB(id, DBNAME+".registers");
    }
    
    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#checkUser(java.lang.String)
     */
    public boolean checkUser(String id) {
	return checkUserFromDB(id, PWDDBNAME+".pwds");
    }

    /**
     * Checks if there is already a row with a certain id in a certain DB
     * 
     * @param id
     *            The id column value to look for
     * @param db
     *            Name of the DB where to look for
     * @return true if it already exists
     */
    private boolean checkUserFromDB(String id, String db){
	Connection conn = null;
	Statement stmt = null;
	boolean result=false;
	try {
	    new org.apache.derby.jdbc.EmbeddedDriver();
	    
	    conn = DriverManager.getConnection(dbURL);
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String selectPWDS="Select id from "+db+" WHERE id='"+id+"'";
	    
	    ResultSet resultSet = stmt.executeQuery(selectPWDS); 
	    result = resultSet.next();

	} catch (Exception e) {
	    Activator.logE("PersistenceDerby.checkUser", "Error checking the database");
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
		Activator.logE("PersistenceDerby.checkUser", "Error checking the database");
		e.printStackTrace();
	    }
	}
	return result;
    }
}

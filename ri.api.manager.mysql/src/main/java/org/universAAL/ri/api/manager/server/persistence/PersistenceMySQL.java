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
public class PersistenceMySQL implements Persistence {

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
    private String dbUSR;
    private String dbPWD;

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#init(org.universAAL.ri.api.manager.RemoteAPI)
     */
    public void init(RemoteAPI remoteAPI) {
	this.api = remoteAPI;
	dbURL = "jdbc:mysql:" + Configuration.getDerbyPath();
	dbUSR=Configuration.getDerbyUser();
	dbPWD=Configuration.getDerbyPass();
	Connection conn = null;
	Statement stmt = null;
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	    if(dbUSR!=null && dbPWD!=null){
		conn = DriverManager.getConnection(dbURL,dbUSR,dbPWD);
	    }else{
		conn = DriverManager.getConnection(dbURL);
	    }
	    
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String createDB = "CREATE DATABASE IF NOT EXISTS " + DBNAME
		    + " DEFAULT CHARACTER SET utf8 ";
	    String createDBPWD = "CREATE DATABASE IF NOT EXISTS " + PWDDBNAME
		    + " DEFAULT CHARACTER SET utf8 ";
	    String createREGISTERS = "CREATE TABLE IF NOT EXISTS " + DBNAME
		    + ".registers ( id VARCHAR(254) PRIMARY KEY NOT NULL, remote VARCHAR(512)," +
		    " tstmp TIMESTAMP, version VARCHAR(25) )";
	    String createSUBSCRIBERS = "CREATE TABLE IF NOT EXISTS " + DBNAME
		    + ".subscribers (rowid INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT " +
		    ", id VARCHAR(254) NOT NULL, pattern VARCHAR(5120)," +
		    " tstmp TIMESTAMP, CONSTRAINT subid_fk FOREIGN KEY (id) REFERENCES "
		    + DBNAME + ".registers(id) ON DELETE CASCADE)";
	    String createCALLEES = "CREATE TABLE IF NOT EXISTS " + DBNAME
		    + ".callees (rowid INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT " +
		    ", id VARCHAR(254) NOT NULL, profile VARCHAR(5120)," +
		    " tstmp TIMESTAMP, CONSTRAINT calid_fk FOREIGN KEY (id) REFERENCES "
		    + DBNAME + ".registers(id) ON DELETE CASCADE)";
	    String createPWDS = "CREATE TABLE IF NOT EXISTS " + PWDDBNAME
		    + ".pwds ( id VARCHAR(254) PRIMARY KEY NOT NULL, pwd VARCHAR(100) )";
	    // create db and tables. I dont trust addBatch. TODO AutoCommit false?
	    stmt.executeUpdate(createDB);
	    stmt.executeUpdate(createREGISTERS);
	    stmt.executeUpdate(createSUBSCRIBERS);
	    stmt.executeUpdate(createCALLEES);
	    stmt.executeUpdate(createDBPWD);
	    stmt.executeUpdate(createPWDS);
	} catch (Exception e) {
	    Activator.logE("PersistenceMySQL.init", "Error creating database");
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
		Activator.logE("PersistenceMySQL.init", "Error creating database");
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
	    // This id already registered -> update remote and t
	    storeREGISTERS = "UPDATE " + DBNAME + ".registers SET remote='"
		    + remote + "', tstmp='" + t + "'"+ (v!=null?", version='"+v+"'":"") +" WHERE id='" + id + "'";
	}else{
	    //New id - > insert all
	    storeREGISTERS = "INSERT INTO " + DBNAME
		    + ".registers (id, remote, tstmp, version) VALUES ('" + id + "','"
		    + remote + "','" + t.toString() + "', " + (v!=null?"'"+v+"'":"NULL") +")";
	    
	}
	executeGeneric(storeREGISTERS);
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#storeSubscriber(java.lang.String, java.lang.String)
     */
    public void storeSubscriber(String id, String pattern) {
	Timestamp t = new Timestamp(System.currentTimeMillis());
	String storeREGISTERS = "INSERT INTO " + DBNAME
		+ ".subscribers (id, pattern, tstmp) VALUES ('" + id + "','"
		+ pattern + "','" + t.toString() + "')";
	executeGeneric(storeREGISTERS);
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#storeCallee(java.lang.String, java.lang.String)
     */
    public void storeCallee(String id, String profile) {
	Timestamp t = new Timestamp(System.currentTimeMillis());
	String storeREGISTERS = "INSERT INTO " + DBNAME
		+ ".callees (id, profile, tstmp) VALUES ('" + id + "','"
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
	    
	    if(dbUSR!=null && dbPWD!=null){
		conn = DriverManager.getConnection(dbURL,dbUSR,dbPWD);
	    }else{
		conn = DriverManager.getConnection(dbURL);
	    }
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);
	    stmt.executeUpdate(sql);

	} catch (Exception e) {
	    Activator.logE("PersistenceMySQL.executeGeneric", "Error writing in the database");
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
		Activator.logE("PersistenceMySQL.executeGeneric", "Error writing in the database");
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

	    if(dbUSR!=null && dbPWD!=null){
		conn = DriverManager.getConnection(dbURL,dbUSR,dbPWD);
	    }else{
		conn = DriverManager.getConnection(dbURL);
	    }
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String deleteCALLEES = "DELETE FROM " + DBNAME + ".callees WHERE id='" + id + "'";
	    String deleteSUBSCRIBERS = "DELETE FROM " + DBNAME + ".subscribers WHERE id='" + id + "'";
	    String deleteREGISTERS = "DELETE FROM " + DBNAME + ".registers WHERE id='" + id + "'";
	    // create tables. Again, put individual try/catch to keep deleting if errors. I dont trust addBatch.
	    stmt.executeUpdate(deleteCALLEES);
	    stmt.executeUpdate(deleteSUBSCRIBERS);
	    stmt.executeUpdate(deleteREGISTERS);// TODO Remove in cascade? Rsult is the same, but maybe faster?
	} catch (Exception e) {
	    Activator.logE("PersistenceMySQL.removeRegister", "Error removing from the database");
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
		Activator.logE("PersistenceMySQL.removeRegister", "Error removing from the database");
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
	    
	    if(dbUSR!=null && dbPWD!=null){
		conn = DriverManager.getConnection(dbURL,dbUSR,dbPWD);
	    }else{
		conn = DriverManager.getConnection(dbURL);
	    }
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String selectREGISTERS="SELECT * FROM "+DBNAME+".registers";
	    String selectSUSBCRIBERS="SELECT * FROM "+DBNAME+".subscribers";
	    String selectCALLEES="SELECT * FROM "+DBNAME+".callees";
	    
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
	    Activator.logE("PersistenceMySQL.restore", "Error restoring from the database");
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
		Activator.logE("PersistenceMySQL.restore", "Error restoring from the database");
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

	    if(dbUSR!=null && dbPWD!=null){
		conn = DriverManager.getConnection(dbURL,dbUSR,dbPWD);
	    }else{
		conn = DriverManager.getConnection(dbURL);
	    }
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    // Remove where t - tstmp > 0 (that is, the date -in days- of the
	    // row is older than the date up to which remove)
	    String deleteCALLEES = "DELETE FROM " + DBNAME
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
		stmt.executeUpdate(deleteCALLEES);
		stmt.executeUpdate(deleteSUBSCRIBERS);
		stmt.executeUpdate(deleteREGISTERS);// TODO Remove in cascade? Rsult is the same, but maybe faster?
	} catch (Exception e) {
	    Activator.logE("PersistenceMySQL.removeOlderThan", "Error cleaning the database");
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
		Activator.logE("PersistenceMySQL.removeOlderThan", "Error cleaning the database");
		e.printStackTrace();
	    }
	}
    }

    /* (non-Javadoc)
     * @see org.universAAL.ri.api.manager.server.persistence.Persistence#storeUserPWD(java.lang.String, java.lang.String)
     */
    public void storeUserPWD(String id, String pwd) {
	String storeREGISTERS = "INSERT INTO " + PWDDBNAME
		+ ".pwds (id, pwd) VALUES ('" + id + "','"
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
	    
	    if(dbUSR!=null && dbPWD!=null){
		conn = DriverManager.getConnection(dbURL,dbUSR,dbPWD);
	    }else{
		conn = DriverManager.getConnection(dbURL);
	    }
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String selectPWDS="SELECT pwd FROM "+PWDDBNAME+".pwds WHERE id='"+id+"'";
	    
	    ResultSet resultSet = stmt.executeQuery(selectPWDS); 
	    while (resultSet.next()){
		String storedpwd=resultSet.getString(1);
		if(storedpwd.equals(pwd)){
		    result=true;
		}
	    }

	} catch (Exception e) {
	    Activator.logE("PersistenceMySQL.checkUserPWD", "Error restoring from the database");
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
		Activator.logE("PersistenceMySQL.checkUserPWD", "Error restoring from the database");
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
	    
	    if(dbUSR!=null && dbPWD!=null){
		conn = DriverManager.getConnection(dbURL,dbUSR,dbPWD);
	    }else{
		conn = DriverManager.getConnection(dbURL);
	    }
	    stmt = conn.createStatement();
	    stmt.setQueryTimeout(30);

	    String selectPWDS="SELECT id FROM "+db+" WHERE id='"+id+"'";
	    
	    ResultSet resultSet = stmt.executeQuery(selectPWDS); 
	    result = resultSet.next();

	} catch (Exception e) {
	    Activator.logE("PersistenceMySQL.checkUser", "Error checking the database");
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
		Activator.logE("PersistenceMySQL.checkUser", "Error checking the database");
		e.printStackTrace();
	    }
	}
	return result;
    }
}

/*
	Copyright 2015 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.ri.rest.manager.server.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.rest.manager.Activator;
import org.universAAL.ri.rest.manager.resources.Callee;
import org.universAAL.ri.rest.manager.resources.Caller;
import org.universAAL.ri.rest.manager.resources.Publisher;
import org.universAAL.ri.rest.manager.resources.Space;
import org.universAAL.ri.rest.manager.resources.Subscriber;
import org.universAAL.ri.rest.manager.server.Configuration;
import org.universAAL.ri.rest.manager.wrappers.CalleeWrapper;
import org.universAAL.ri.rest.manager.wrappers.CallerWrapper;
import org.universAAL.ri.rest.manager.wrappers.PublisherWrapper;
import org.universAAL.ri.rest.manager.wrappers.SpaceWrapper;
import org.universAAL.ri.rest.manager.wrappers.SubscriberWrapper;
import org.universAAL.ri.rest.manager.wrappers.UaalWrapper;

/**
 * An implementation of the Persistence interface using Apache Derby SQL
 * Database.
 * 
 * @author alfiva
 * 
 */
public class PersistenceDerby implements Persistence {

	private static final String DB_MAIN = "RESTDB";
	private static final String DB_PWDS = "PWDRESTDB";
	private static final String T_SPACES = DB_MAIN + ".SPACES";
	private static final String T_CALLEES = DB_MAIN + ".CALLEES";
	private static final String T_CALLERS = DB_MAIN + ".CALLERS";
	private static final String T_SUBSCRIBERS = DB_MAIN + ".SUBSCRIBERS";
	private static final String T_PUBLISHERS = DB_MAIN + ".PUBLISHERS";
	private static final String T_PWDS = DB_PWDS + ".PWDS";

	private String dbURL;

	// __________INTERFACE Persistence____________

	public void init(ModuleContext context) {
		dbURL = "jdbc:derby:" + Configuration.getDBPath(context);
		String dbUSR = Configuration.getDBUser();
		String dbPWD = Configuration.getDBPass();
		if (dbUSR != null && dbPWD != null) {
			dbURL += ";user=" + dbUSR + ";password=" + dbPWD;
		}
		String bootURL = dbURL + ";create=true;dataEncryption=true;bootPassword=" + Configuration.getDBKey();
		Connection conn = null;
		Statement stmt = null;
		try {
			new org.apache.derby.jdbc.EmbeddedDriver();

			conn = DriverManager.getConnection(bootURL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);

			String execSpace = "CREATE TABLE " + T_SPACES
					+ " ( id varchar(512) NOT NULL , callback varchar(512), tstmp timestamp, version varchar(25), PRIMARY KEY (id) )";
			String execSub = "CREATE TABLE " + T_SUBSCRIBERS
					+ " ( id varchar(512) NOT NULL, subid varchar(512) NOT NULL, serial varchar(5120) NOT NULL, callback varchar(512), tstmp timestamp, PRIMARY KEY (id,subid), CONSTRAINT sub_fk FOREIGN KEY (id) REFERENCES "
					+ T_SPACES + "(id))";
			String execPub = "CREATE TABLE " + T_PUBLISHERS
					+ " ( id varchar(512) NOT NULL, subid varchar(512) NOT NULL, serial varchar(5120) NOT NULL, tstmp timestamp, PRIMARY KEY (id,subid), CONSTRAINT pub_fk FOREIGN KEY (id) REFERENCES "
					+ T_SPACES + "(id))";
			String execCee = "CREATE TABLE " + T_CALLEES
					+ " ( id varchar(512) NOT NULL, subid varchar(512) NOT NULL, serial varchar(5120) NOT NULL, callback varchar(512), tstmp timestamp, PRIMARY KEY (id,subid), CONSTRAINT cee_fk FOREIGN KEY (id) REFERENCES "
					+ T_SPACES + "(id))";
			String execCer = "CREATE TABLE " + T_CALLERS
					+ " ( id varchar(512) NOT NULL, subid varchar(512) NOT NULL, tstmp timestamp, PRIMARY KEY (id,subid), CONSTRAINT cer_fk FOREIGN KEY (id) REFERENCES "
					+ T_SPACES + "(id))";
			String execPwd = "CREATE TABLE " + T_PWDS + " ( id varchar(512) PRIMARY KEY NOT NULL, pwd  varchar(100) )";

			// create tables. Put individual try/catch to ignore already
			// created. I dont trust addBatch.
			try {
				stmt.executeUpdate(execSpace);
			} catch (SQLException e) {
				if (e.getSQLState().equals("X0Y32")) {
					Activator.logI("PersistenceDerby.init", "Database already exists, spaces");
				} else {
					Activator.logE("PersistenceDerby.init", "Error creating database, spaces");
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execSub);
			} catch (SQLException e) {
				if (e.getSQLState().equals("X0Y32")) {
					Activator.logI("PersistenceDerby.init", "Database already exists, subscribers");
				} else {
					Activator.logE("PersistenceDerby.init", "Error creating database, subscribers");
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execPub);
			} catch (SQLException e) {
				if (e.getSQLState().equals("X0Y32")) {
					Activator.logI("PersistenceDerby.init", "Database already exists, publishers");
				} else {
					Activator.logE("PersistenceDerby.init", "Error creating database, publishers");
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execCee);
			} catch (SQLException e) {
				if (e.getSQLState().equals("X0Y32")) {
					Activator.logI("PersistenceDerby.init", "Database already exists, callees");
				} else {
					Activator.logE("PersistenceDerby.init", "Error creating database, callees");
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execCer);
			} catch (SQLException e) {
				if (e.getSQLState().equals("X0Y32")) {
					Activator.logI("PersistenceDerby.init", "Database already exists, callers");
				} else {
					Activator.logE("PersistenceDerby.init", "Error creating database, callers");
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execPwd);
			} catch (SQLException e) {
				if (e.getSQLState().equals("X0Y32")) {
					Activator.logI("PersistenceDerby.init", "Database already exists, pwds");
				} else {
					Activator.logE("PersistenceDerby.init", "Error creating database, pwds");
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

	public void storeSpace(Space s, String v) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		String storeREGISTERS;
		if (checkIdExists(s.getId(), T_SPACES)) {
			storeREGISTERS = "update " + T_SPACES + " SET callback='" + s.getCallback() + "', tstmp='" + t + "'"
					+ (v != null ? ", version='" + v + "'" : "") + " WHERE id='" + s.getId() + "'";
		} else {
			storeREGISTERS = "insert into " + T_SPACES + " (id, callback, tstmp, version) values ('" + s.getId() + "','"
					+ s.getCallback() + "','" + t + "', " + (v != null ? "'" + v + "'" : "NULL") + ")";
		}
		executeGeneric(storeREGISTERS);
	}

	public void storeSubscriber(String space, Subscriber s) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		String exec;
		if (checkSubIdExists(space, s.getId(), T_SUBSCRIBERS)) {
			exec = "update " + T_SUBSCRIBERS + " SET serial='" + s.getPattern() + "', callback='" + s.getCallback()
					+ "', tstmp='" + t + "' WHERE id='" + space + "' AND subid='" + s.getId() + "'";
		} else {
			exec = "insert into " + T_SUBSCRIBERS + " (id, subid, serial, callback, tstmp) values ('" + space + "','"
					+ s.getId() + "','" + s.getPattern() + "','" + s.getCallback() + "','" + t + "')";
		}
		executeGeneric(exec);
	}

	public void removeSubscriber(String space, String subid) {
		if (checkSubIdExists(space, subid, T_SUBSCRIBERS)) {
			String exec = "delete from " + T_SUBSCRIBERS + " WHERE id='" + space + "' AND subid='" + subid + "'";
			executeGeneric(exec);
		}
	}

	public void storePublisher(String space, Publisher p) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		String exec;
		if (checkSubIdExists(space, p.getId(), T_PUBLISHERS)) {
			exec = "update " + T_PUBLISHERS + " SET serial='" + p.getProviderinfo() + "', tstmp='" + t + "' WHERE id='"
					+ space + "' AND subid='" + p.getId() + "'";
		} else {
			exec = "insert into " + T_PUBLISHERS + " (id, subid, serial, tstmp) values ('" + space + "','" + p.getId()
					+ "','" + p.getProviderinfo() + "','" + t + "')";
		}
		executeGeneric(exec);
	}

	public void removePublisher(String space, String subid) {
		if (checkSubIdExists(space, subid, T_PUBLISHERS)) {
			String exec = "delete from " + T_PUBLISHERS + " WHERE id='" + space + "' AND subid='" + subid + "'";
			executeGeneric(exec);
		}
	}

	public void storeCaller(String space, Caller c) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		String exec;
		if (checkSubIdExists(space, c.getId(), T_CALLERS)) {
			exec = "update " + T_CALLERS + " SET tstmp='" + t + "' WHERE id='" + space + "' AND subid='" + c.getId()
					+ "'";
		} else {
			exec = "insert into " + T_CALLERS + " (id, subid, tstmp) values ('" + space + "','" + c.getId() + "','" + t
					+ "')";
		}
		executeGeneric(exec);
	}

	public void removeCaller(String space, String subid) {
		if (checkSubIdExists(space, subid, T_CALLERS)) {
			String exec = "delete from " + T_CALLERS + " WHERE id='" + space + "' AND subid='" + subid + "'";
			executeGeneric(exec);
		}
	}

	public void storeCallee(String space, Callee c) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		String exec;
		if (checkSubIdExists(space, c.getId(), T_CALLEES)) {
			exec = "update " + T_CALLEES + " SET serial='" + c.getProfile() + "', callback='" + c.getCallback()
					+ "', tstmp='" + t + "' WHERE id='" + space + "' AND subid='" + c.getId() + "'";
		} else {
			exec = "insert into " + T_CALLEES + " (id, subid, serial, callback, tstmp) values ('" + space + "','"
					+ c.getId() + "','" + c.getProfile() + "','" + c.getCallback() + "','" + t + "')";
		}
		executeGeneric(exec);
	}

	public void removeCallee(String space, String subid) {
		if (checkSubIdExists(space, subid, T_CALLEES)) {
			String exec = "delete from " + T_CALLEES + " WHERE id='" + space + "' AND subid='" + subid + "'";
			executeGeneric(exec);
		}
	}

	public void removeSpace(String s) {
		Connection conn = null;
		Statement stmt = null;
		try {
			new org.apache.derby.jdbc.EmbeddedDriver();

			conn = DriverManager.getConnection(dbURL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);

			String execCee = "delete from " + T_CALLEES + " WHERE id='" + s + "'";
			String execSub = "delete from " + T_SUBSCRIBERS + " WHERE id='" + s + "'";
			String execCer = "delete from " + T_CALLERS + " WHERE id='" + s + "'";
			String execPub = "delete from " + T_PUBLISHERS + " WHERE id='" + s + "'";
			String execSpace = "delete from " + T_SPACES + " WHERE id='" + s + "'";
			// Again, put individual try/catch to keep deleting if errors. I
			// dont trust addBatch.
			try {
				stmt.executeUpdate(execCee);
			} catch (SQLException e) {
				if (!e.getSQLState().equals("X0Y32")) {
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execCer);
			} catch (SQLException e) {
				if (!e.getSQLState().equals("X0Y32")) {
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execPub);
			} catch (SQLException e) {
				if (!e.getSQLState().equals("X0Y32")) {
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execSub);
			} catch (SQLException e) {
				if (!e.getSQLState().equals("X0Y32")) {
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execSpace);// TODO Remove in cascade? Rsult
												// is the same, but maybe
												// faster?
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

	public void restore() {
		Connection conn = null;
		Statement stmt = null;
		try {
			new org.apache.derby.jdbc.EmbeddedDriver();

			conn = DriverManager.getConnection(dbURL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);

			String execSpace = "Select * from " + T_SPACES;
			String execSub = "Select * from " + T_SUBSCRIBERS;
			String execCee = "Select * from " + T_CALLEES;
			String execPub = "Select * from " + T_PUBLISHERS;
			String execCer = "Select * from " + T_CALLERS;

			ResultSet resultSet = stmt.executeQuery(execSpace);
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String callback = resultSet.getString("callback");
				if (id != null) {
					UaalWrapper.getInstance().addTenant(new SpaceWrapper(new Space(id, callback)));
					;
				}
			}
			// No need to link from id to id between tables,
			// just iterate once everything is registered
			resultSet = stmt.executeQuery(execSub);
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String subid = resultSet.getString("subid");
				String serial = resultSet.getString("serial");
				String callback = resultSet.getString("callback");
				if (id != null && subid != null && serial != null) {
					ContextEventPattern cep = (ContextEventPattern) Activator.getParser().deserialize(serial);
					UaalWrapper.getInstance().getTenant(id).addContextSubscriber(
							new SubscriberWrapper(Activator.getUaalContext(), new ContextEventPattern[] { cep },
									new Subscriber(id, subid, callback, serial), id));
				}
			}

			resultSet = stmt.executeQuery(execCee);
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String subid = resultSet.getString("subid");
				String serial = resultSet.getString("serial");
				String callback = resultSet.getString("callback");
				if (id != null && subid != null && serial != null) {
					ServiceProfile sp = (ServiceProfile) Activator.getParser().deserialize(serial);
					UaalWrapper.getInstance().getTenant(id)
							.addServiceCallee(new CalleeWrapper(Activator.getUaalContext(), new ServiceProfile[] { sp },
									new Callee(id, subid, callback, serial), id));
				}
			}

			resultSet = stmt.executeQuery(execCer);
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String subid = resultSet.getString("subid");
				if (id != null && subid != null) {
					UaalWrapper.getInstance().getTenant(id)
							.addServiceCaller(new CallerWrapper(Activator.getUaalContext(), new Caller(id, subid)));
				}
			}

			resultSet = stmt.executeQuery(execPub);
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String subid = resultSet.getString("subid");
				String serial = resultSet.getString("serial");
				if (id != null && subid != null && serial != null) {
					ContextProvider cp = (ContextProvider) Activator.getParser().deserialize(serial);
					UaalWrapper.getInstance().getTenant(id).addContextPublisher(
							new PublisherWrapper(Activator.getUaalContext(), cp, new Publisher(id, subid, serial)));
				}
			}

		} catch (Exception e) {
			Activator.logE("PersistenceDerby.restore", "Error restoring from the database");
			e.printStackTrace();
		} finally {
			try {// javadoc: resultSet auto closed if stmt is closed
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

	public void storeUserPWD(String id, String pwd) {
		String storeREGISTERS = "insert into " + T_PWDS + " (id, pwd) values ('" + id + "','" + pwd + "')";
		executeGeneric(storeREGISTERS);
	}

	public boolean checkUserPWD(String id, String pwd) {
		Connection conn = null;
		Statement stmt = null;
		boolean result = false;
		try {
			new org.apache.derby.jdbc.EmbeddedDriver();

			conn = DriverManager.getConnection(dbURL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);

			String selectPWDS = "Select pwd from " + T_PWDS + " WHERE id='" + id + "'";

			ResultSet resultSet = stmt.executeQuery(selectPWDS);
			while (resultSet.next()) {
				String storedpwd = resultSet.getString(1);
				if (storedpwd.equals(pwd)) {
					result = true;
				}
			}

		} catch (Exception e) {
			Activator.logE("PersistenceDerby.checkUserPWD", "Error restoring from the database");
			e.printStackTrace();
		} finally {
			try {// javadoc: resultSet auto closed if stmt is closed
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

	public boolean checkUser(String id) {
		return checkIdExists(id, T_PWDS);
	}

	// __________UTILITY____________

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
		if (millis < 0l)
			return;// Disable with millis <0
		Timestamp t = new Timestamp(System.currentTimeMillis() - millis);// Date
																			// up
																			// to
																			// which
																			// remove
		Connection conn = null;
		Statement stmt = null;
		try {
			new org.apache.derby.jdbc.EmbeddedDriver();

			conn = DriverManager.getConnection(dbURL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);

			// Remove where t - tstmp > 0 (that is, the date -in days- of the
			// row is older than the date up to which remove)
			String execCee = "delete from " + T_CALLEES + " WHERE {fn TIMESTAMPDIFF( SQL_TSI_DAY, tstmp, '"
					+ t.toString() + "')} > 0";
			String execSub = "delete from " + T_SUBSCRIBERS + " WHERE {fn TIMESTAMPDIFF( SQL_TSI_DAY, tstmp, '"
					+ t.toString() + "')} > 0";
			String execCer = "delete from " + T_CALLERS + " WHERE {fn TIMESTAMPDIFF( SQL_TSI_DAY, tstmp, '"
					+ t.toString() + "')} > 0";
			String execPub = "delete from " + T_PUBLISHERS + " WHERE {fn TIMESTAMPDIFF( SQL_TSI_DAY, tstmp, '"
					+ t.toString() + "')} > 0";
			String execSpace = "delete from " + T_SPACES + " WHERE {fn TIMESTAMPDIFF( SQL_TSI_DAY, tstmp, '"
					+ t.toString() + "')} > 0";
			// create tables. Again, put individual try/catch to keep deleting
			// if errors. I dont trust addBatch.
			try {
				stmt.executeUpdate(execCee);
			} catch (SQLException e) {
				if (!e.getSQLState().equals("X0Y32")) {
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execCer);
			} catch (SQLException e) {
				if (!e.getSQLState().equals("X0Y32")) {
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execSub);
			} catch (SQLException e) {
				if (!e.getSQLState().equals("X0Y32")) {
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execPub);
			} catch (SQLException e) {
				if (!e.getSQLState().equals("X0Y32")) {
					e.printStackTrace();
				}
			}
			try {
				stmt.executeUpdate(execSpace);// TODO Remove in cascade? Rsult
												// is the same, but maybe
												// faster?
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

	private boolean checkIdExists(String id, String table) {
		Connection conn = null;
		Statement stmt = null;
		boolean result = false;
		try {
			new org.apache.derby.jdbc.EmbeddedDriver();

			conn = DriverManager.getConnection(dbURL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);

			String selectPWDS = "Select id from " + table + " WHERE id='" + id + "'";

			ResultSet resultSet = stmt.executeQuery(selectPWDS);
			result = resultSet.next();

		} catch (Exception e) {
			Activator.logE("PersistenceDerby.checkUser", "Error checking the database");
			e.printStackTrace();
		} finally {
			try {// javadoc: resultSet auto closed if stmt is closed
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

	private boolean checkSubIdExists(String id, String subid, String table) {
		Connection conn = null;
		Statement stmt = null;
		boolean result = false;
		try {
			new org.apache.derby.jdbc.EmbeddedDriver();

			conn = DriverManager.getConnection(dbURL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);

			String selectPWDS = "Select id from " + table + " WHERE id='" + id + "' AND subid='" + subid + "'";

			ResultSet resultSet = stmt.executeQuery(selectPWDS);
			result = resultSet.next();

		} catch (Exception e) {
			Activator.logE("PersistenceDerby.checkUser", "Error checking the database");
			e.printStackTrace();
		} finally {
			try {// javadoc: resultSet auto closed if stmt is closed
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

	private void executeGeneric(String sql) {
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

}

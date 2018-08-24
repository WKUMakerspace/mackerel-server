package edu.wku.makerspace.mackerel.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Properties;

public class DBConn {
	private static Connection conn = null;
	private static Properties props = new Properties();
	private static Statement st = null;
	
	/**
	 * Performs a query if connected to a database and returns the output, if any.
	 * @param query
	 * @return
	 */
	public static synchronized ResultSet query(String query) {
		ResultSet ret = null;
		if (conn != null ) {
			try {
				if (st != null) st.close();
				st = conn.createStatement();
				System.out.println("Querying DB: " + query);
				String type = query.substring(0, query.indexOf(' '));
				if (type.equalsIgnoreCase("INSERT") || type.equalsIgnoreCase("UPDATE") || type.equalsIgnoreCase("DELETE")) {
					st.executeUpdate(query);
				} else {
					ret = st.executeQuery(query);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	
	public static String updateRecords(String table, String toset, String... conditions) {
		String q = "UPDATE " + table + " SET " + toset + " WHERE " + conditions[0];
		for (int i = 1; i < conditions.length; i++) {
			q = q + " AND " + conditions[i];
		}
		return q;
	}
	
	/**
	 * Returns user information from the 'users' table in the database.
	 * This is a 3-string table: last name, first name, and phone number.
	 * @param userid
	 * @return
	 */
	public static String[] checkUser(String userid) {
		ResultSet check = query("SELECT * FROM users WHERE wku_id='" + userid + "'");
		try {
			if (check.next()) {
				String[] ret = new String[3];
				for (int i = 0; i < 3; i++) {
					ret[i] = check.getString(i+2);
				}
				return ret;
			}
		} catch (SQLException e) {
			//e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Checks the what permission level a user has for the specified machine/area.
	 * Returns 0 if no permission
	 * Returns 1 if basic training complete
	 * Returns 2 if advanced training complete
	 * Returns 3 if staff level
	 * Returns -1 if not found.
	 * @param userid
	 * @param numperm
	 * @return
	 */
	public static int checkPerms(String userid, int numperm) {
		int permInt = ((numperm*2) / 30) + 1;
		int permNum = (numperm*2) % 30;
		ResultSet check = query("SELECT * FROM permissions WHERE wku_id='" + userid + "'");
		try {
			if (check.next()) {
				int permVar = check.getInt(permInt + 1);
				int perm = (permVar >> permNum) & 3;
				return perm;
			}
		} catch (SQLException e) {
			//e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Signs a user into the system.
	 * @param userid
	 */
	public static boolean signin(String userid, String desc) {
		String date = LocalDate.now().toString(); //YYYY-MM-DD
		String time = LocalTime.now().toString().substring(0,8); //hh:mm:ss
		ResultSet today = query("SELECT * FROM attendance WHERE date='" + date + "'");
		try {
			while (today.next()) {
				if (today.getTime(4) == null && userid.equals("" + today.getInt(1))) {
					//userid already signed in!
					return false;
				}
			}
			String q = "INSERT INTO attendance (wku_id, date, time_in";
			if (desc == null) {
				q += ") VALUES ('" + userid + "','" + date + "','" + time + "')";
			} else {
				q += ", description) VALUES ('" + userid + "','" + date + "','" + time + "','" + desc + "')";
			}
			query(q);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Signs a user out of the system.
	 * @param userid
	 */
	public static boolean signout(String userid) {
		String date = LocalDate.now().toString(); //YYYY-MM-DD
		String time = LocalTime.now().toString().substring(0,8); //hh:mm:ss
		ResultSet today = query("SELECT * FROM attendance WHERE date='" + date + "' ORDER BY time_in DESC");
		try {
			while (today.next()) {
				if (today.getTime(4) == null && userid.equals("" + today.getInt(1))) {
					query("UPDATE attendance SET time_out='" + time + "' WHERE wku_id='" + userid + "' AND date='" + date + "' AND time_in='" + today.getTime(3) + "'");
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Connects to specified database, returns false if it fails.
	 * @param user
	 * @param address
	 * @param passwd
	 * @return
	 */
	public static boolean connect(String user, String address, String passwd) {
		try {
			DriverManager.setLoginTimeout(10);
			props.put("user", user);
		    props.put("password", passwd);
		    if (conn != null) conn.close();
		    //Modify this to suit whatever database system you use
			conn = DriverManager.getConnection("jdbc:mysql://" + address + "/", props);
		} catch (SQLException e) {
			//e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Attempts to disconnect from the current database, if connected.
	 */
	public static void disconnect() {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			return;
		}
	}
}

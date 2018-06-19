package edu.wku.makerspace.mackerel.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class Calendar {
	private static boolean isBeingAccessed = false;
	private static ArrayList<Appointment> apps;
	
	//public static synchronized 
	
	/**
	 * Reloads the calendar with all appointments for the current day from the database.
	 */
	public static void reload() {
		while (isBeingAccessed) {} //wait
		isBeingAccessed = true;
		System.out.println("Updating appointment calendar.");
		String date = LocalDate.now().toString();
		ResultSet set = DBConn.query("SELECT * FROM appointments WHERE date='" + date + "' ORDER BY time_in ASC");
		try {
			while (set.next()) {
				//create a new appointment
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		isBeingAccessed = false;
	}
	
	class Appointment {
		private String wku_id;
		private String date;
		private String time_in;
		private String machine_id;
		private String description;
		private String status;
		
		public Appointment(String wid, String d, String t, String mid, String desc, String stat) {
			wku_id = wid;
			date = d;
			time_in = t;
			machine_id = mid;
			description = desc;
			status = stat;
		}
	}
}
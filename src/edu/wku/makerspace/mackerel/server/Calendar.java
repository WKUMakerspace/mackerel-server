package edu.wku.makerspace.mackerel.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class Calendar {
	private static boolean isBeingAccessed = false;
	private static ArrayList<Appointment> appts;
	
	//public static synchronized 
	
	
	public static Appointment checkForUpcomingAppointment(String machine_id) {
		//double d = hoursUntil("14:00:00");
		return null;
	}
	
	/**
	 * Broadcasts that an update happened to all kiosk nodes.
	 */
	public static void broadcastUpdate() {
		Node[] nodes = NodeServer.getNodeList();
		for (Node n : nodes) {
			if (n != null) {
				if (n instanceof KioskNode && n.isRunning()) {
					n.send("APPT_HEADER;" + appts.size());
				}
			}
		}
	}
	
	public static void sendApptToNode(int id, Node node) {
		while (isBeingAccessed) {} //wait
		isBeingAccessed = true;
		for (Appointment appt : appts) {
			
		}
		isBeingAccessed = false;
	}
	
	/**
	 * Reloads the calendar with all appointments for the current day from the database.
	 */
	public static void reload() {
		while (isBeingAccessed) {} //wait
		isBeingAccessed = true;
		System.out.println("Updating appointment calendar.");
		ArrayList<Appointment> apps_new = new ArrayList<Appointment>();
		String date = LocalDate.now().toString();
		ResultSet set = DBConn.query("SELECT * FROM appointments WHERE date='" + date + "' ORDER BY time_in ASC");
		try {
			while (set.next()) {
				//create a new appointment
				String[] info = new String[7];
				for (int i = 0; i < 7; i++) {
					info[i] = set.getString(i+1);
				}
				try {
					Appointment app = new Appointment(info);
					apps_new.add(app);
				} catch (Exception e) {
					e.printStackTrace();
					//appt is BAD, do not add. this shouldn't happen anyway...
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			isBeingAccessed = false;
			return;
		}
		
		//broadcast update to appointments.
		broadcastUpdate();
		
		appts = apps_new;
		isBeingAccessed = false;
	}
	
	/**
	 * Returns the amount of time left (in hours) until the given time.
	 * @param time
	 * @return
	 */
	private static double hoursUntil(String time) {
		double until = 0;
		LocalTime then = LocalTime.parse(time);
		LocalTime now = LocalTime.now();
		until = until + (then.getHour() - now.getHour());
		until = until + (double)(then.getMinute() - now.getMinute())/60.0;
		until = until + (double)(then.getSecond() - now.getSecond())/3600.0;
		return until;
	}
}
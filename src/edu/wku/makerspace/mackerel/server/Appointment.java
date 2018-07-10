package edu.wku.makerspace.mackerel.server;

public class Appointment {
	private String wku_id;
	private String date;
	private String time_in;
	private double est_len;
	private String machine_id;
	private String description;
	private String status;
	
	public Appointment(String[] info) {
		wku_id = info[0];
		date = info[1];
		time_in = info[2];
		est_len = Double.parseDouble(info[3]);
		machine_id = info[4];
		description = info[5];
		status = info[6];
	}
	
	public String getWKUID() {
		return wku_id;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getTime() {
		return time_in;
	}
	
	public double getEstimatedLength() {
		return est_len;
	}
	
	public String getMachineID() {
		return machine_id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getStatus() {
		return status;
	}
	
	/**
	 * Changes the date of the appointment.
	 * @param new_date
	 */
	public void setDate(String new_date) {
		DBConn.updateRecords("appointments", "date="+new_date, "wku_id="+wku_id, "date="+date, "time_in="+time_in);
		date = new_date;
	}
	
	/**
	 * Changes the time of the appointment.
	 * @param new_time
	 */
	public void setTime(String new_time) {
		DBConn.updateRecords("appointments", "time_in="+new_time, "wku_id="+wku_id, "date="+date, "time_in="+time_in);
		time_in = new_time;
	}
	
	/**
	 * Changes the status of the appointment.
	 * @param new_status
	 */
	public void setStatus(String new_status) {
		status = new_status;
		DBConn.updateRecords("appointments", "status="+new_status, "wku_id="+wku_id, "date="+date, "time_in="+time_in);
	}
}
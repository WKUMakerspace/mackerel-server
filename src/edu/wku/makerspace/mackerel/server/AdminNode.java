package edu.wku.makerspace.mackerel.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminNode extends Node {
	private boolean accessGranted;
	
	public AdminNode(Socket newsock, String newnid, BufferedReader newin, PrintWriter newout) {
		super(newsock, newnid, newin, newout);
		accessGranted = false;
		send("SECRET_REQ");
	}
	
	/**
	 * Handles query response and sends over socket in a readable format.
	 * @param args
	 */
	private void handleQuery(String[] args) {
		String query = args[0];
		for (int i = 1; i < args.length; i++) {
			query = query + " " + args[i];
		}
		ResultSet set = DBConn.query(query);
		if (set != null) {
			try {
				while (set.next()) {
					String line = "DB_RESPONSE;ENTRY";
					boolean lineNotDone = true;
					int count = 0;
					while (lineNotDone) {
						count++;
						try {
							line = line + ";" + set.getString(count);
						} catch (SQLException e) {
							lineNotDone = false;
						}
					}
					send(line);
				}
			} catch (SQLException e) {
				//e.printStackTrace();
			}
			send("DB_RESPONSE;END");
		} else {
			send("DB_RESPONSE;FAILED");
		}
	}
	
	@Override
	protected void onRecv(String message, String[] args) {
		if (message.equals("SECRET_ANS")) {
			if (args[0] == ConfigReader.getOption("ac_secret")) {
				//grant access!
				accessGranted = true;
				send("SECRET_SUCCESS");
			} else {
				send("SECRET_FAILURE");
			}
		}
		if (message.equals("QUERY") && accessGranted) handleQuery(args);
		if (accessGranted) super.onRecv(message, args);
	}
}

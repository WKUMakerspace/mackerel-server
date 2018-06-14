package edu.wku.makerspace.mackerel.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class KioskNode extends Node {
	public KioskNode(Socket newsock, String newnid, BufferedReader newin, PrintWriter newout) {
		super(newsock, newnid, newin, newout);
	}
	
	/**
	 * Processes a sign-in request from the kiosk client.
	 * @param userid
	 */
	private void signin(String userid) {
		if (DBConn.signin(userid)) {
			send("RESP_SUCCESS");
		} else {
			send("RESP_FAILURE");
		}
	}
	
	/**
	 * Processes a sign-out request from the kiosk client.
	 * @param userid
	 */
	private void signout(String userid) {
		for (Node n : NodeServer.getNodeList()) {
			if (n instanceof ToolNode) {
				//handle signout on fellow nodes
				
			}
		}
		if (DBConn.signout(userid)) {
			send("RESP_SUCCESS");
		} else {
			send("RESP_FAILURE");
		}
	}
	
	@Override
	protected void onRecv(String message, String[] args) {
		if (message.equals("SIGNIN")) {
			signin(args[0]);
		}
		if (message.equals("SIGNOUT")) {
			signout(args[0]);
		}
		if (message.equals("USER_CREATE")) {
			if (DBConn.checkUser(args[0]) != null) {
				send("RESP_FAILURE");
			} else {
				try {
					String q = "INSERT INTO users (wku_id, lastname, firstname) VALUES ('"+args[0]+"','"+args[1]+"','"+args[2]+"')";
					if (args.length > 3) {
						q = "INSERT INTO users (wku_id, lastname, firstname, phone) VALUES ('"+args[0]+"','"+args[1]+"','"+args[2]+"','"+args[3]+"')";
					}
					DBConn.query(q);
				} catch (Exception e) {
					//e.printStackTrace();
					send("RESP_FAILURE");
				}
				if (DBConn.checkUser(args[0]) != null) {
					send("RESP_SUCCESS");
				} else {
					send("RESP_FAILURE");
				}
			}
		}
		super.onRecv(message, args);
	}
}

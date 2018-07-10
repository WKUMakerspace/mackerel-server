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
			if (n != null) {
				if (n instanceof ToolNode) {
					//handle signout on fellow nodes
					if (userid == ((ToolNode)n).getActiveUser()) {
						//((ToolNode)n).advanceQueue();
					}
				}
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
			signin(xor(args[0]));
		}
		if (message.equals("SIGNOUT")) {
			signout(xor(args[0]));
		}
		if (message.equals("USER_CREATE")) {
			if (DBConn.checkUser(xor(args[0])) != null) {
				send("RESP_FAILURE;USER_EXISTS");
			} else {
				try {
					String q = "INSERT INTO users (wku_id, lastname, firstname) VALUES ('"+xor(args[0])+"','"+args[1]+"','"+args[2]+"')";
					if (args.length > 3) {
						q = "INSERT INTO users (wku_id, lastname, firstname, phone) VALUES ('"+xor(args[0])+"','"+args[1]+"','"+args[2]+"','"+args[3]+"')";
					}
					DBConn.query(q);
				} catch (Exception e) {
					//e.printStackTrace();
					send("RESP_FAILURE");
				}
				if (DBConn.checkUser(xor(args[0])) != null) {
					send("RESP_SUCCESS");
				} else {
					send("RESP_FAILURE");
				}
			}
		}
		if (message.equals("USER_MODIFY")) {
			if (DBConn.checkUser(xor(args[0])) != null) {
				try {
					String q = "UPDATE users SET lastname="+args[1]+", firstname="+args[2];
					if (args.length > 3) {
						q = q + ", phone=" + args[3];
					}
					q = q + " WHERE wku_id=" + xor(args[0]);
					DBConn.query(q);
				} catch (Exception e) {
					//e.printStackTrace();
					send("RESP_FAILURE");
				}
			} else {
				send("RESP_FAILURE;USER_DOES_NOT_EXIST");
			}
		}
		if (message.equals("QUEUE_CHECK")) {
			if (args[0].equals("ALL")) {
				//will send all queues from all ToolNode instances
				
			} else {
				//args[0] should be the nid of a ToolNode instance
				
			}
		}
		super.onRecv(message, args);
	}
}

package edu.wku.makerspace.mackerel.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ToolNode extends Node {
	//user queue, holds student ids
	private Queue<String> queue;
	
	protected String userIdUsing;
	protected boolean inUse;
	
	public ToolNode(Socket newsock, String newnid, BufferedReader newin, PrintWriter newout) {
		super(newsock, newnid, newin, newout);
		userIdUsing = "NONE";
		inUse = false;
		queue = new LinkedList<>();
	}
	
	/**
	 * Returns the student ID of the current active user.
	 * @return
	 */
	public String getActiveUser() {
		return userIdUsing;
	}
	
	/**
	 * Sets the active user to the student ID given.
	 * @param userid
	 * @return
	 */
	protected boolean setActiveUser(String userid) {
		if (!inUse) {
			userIdUsing = userid;
			return true;
		} else return false;
	}
	
	@Override
	protected void onRecv(String message, String[] args) {
		if (message.equals("TOOL_USER_CHANGE")) {
			if (DBConn.checkUser(args[0]) != null) {
				setActiveUser(args[0]);
				send("RESP_SUCCESS");
			} else {
				send("RESP_FAILURE");
			}
		}
		if (message.equals("QUEUE_ADD")) {
			//adds a student id to the queue
			queue.add(args[0]);
			send("RESP_SUCCESS");
		}
		if (message.equals("QUEUE_REMOVE")) {
			//removes a student id from the queue
			if (queue.remove(args[0])) {
				send("RESP_SUCCESS");
			} else send("RESP_FAILURE");
		}
		if (message.equals("QUEUE_READ")) {
			//reads the queue
		}
		super.onRecv(message, args);
	}
}

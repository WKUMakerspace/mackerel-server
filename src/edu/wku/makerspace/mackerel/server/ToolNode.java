package edu.wku.makerspace.mackerel.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ToolNode extends Node {
	//user queue, holds student ids
	private Queue<String> queue;
	private boolean accessing;
	
	protected String userIdUsing;
	protected boolean inUse;
	
	public ToolNode(Socket newsock, String newnid, BufferedReader newin, PrintWriter newout) {
		super(newsock, newnid, newin, newout);
		userIdUsing = "NONE";
		inUse = false;
		queue = new LinkedList<String>();
		accessing = false;
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
			if (userIdUsing != "NONE") {
				inUse = true;
			} else {
				inUse = false;
			}
			return true;
		} else return false;
	}
	
	/**
	 * Advances the queue to the next person.
	 */
	public void advanceQueue() {
		waitUntilAccessible();
		accessing = true;
		if (queue.peek() == null) {
			setActiveUser("NONE");
		} else {
			setActiveUser(queue.poll());
		}
		accessing = false;
	}
	
	/**
	 * Returns all the users in the queue, or null if there is nobody waiting.
	 * @return
	 */
	public String[] getQueueContents() {
		waitUntilAccessible();
		accessing = true;
		LinkedList<String> list = (LinkedList<String>)queue; //needed to access all the elements
		if (list.size() < 1) return null; //queue is empty
		String[] ret = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = list.get(i);
		}
		accessing = false;
		return ret;
	}
	
	/**
	 * Waits until the accessing variable is false, preventing concurrent access to the queue.
	 */
	private void waitUntilAccessible() {
		while (accessing) {}
	}
	
	@Override
	protected void onRecv(String message, String[] args) {
		if (message.equals("TOOL_USER_CHANGE")) {
			if (DBConn.checkUser(xor(args[0])) != null) {
				setActiveUser(xor(args[0]));
				send("RESP_SUCCESS");
			} else {
				send("RESP_FAILURE");
			}
		}
		if (message.equals("QUEUE_ADD")) {
			//adds a student id to the queue
			waitUntilAccessible();
			accessing = true;
			queue.add(xor(args[0]));
			accessing = false;
			send("RESP_SUCCESS");
		}
		if (message.equals("QUEUE_REMOVE")) {
			//removes a student id from the queue
			waitUntilAccessible();
			accessing = true;
			boolean flag = queue.remove(xor(args[0]));
			accessing = false;
			if (flag) {
				send("RESP_SUCCESS");
			} else {
				send("RESP_FAILURE;NOT_IN_QUEUE");
			}
		}
		if (message.equals("QUEUE_ADVANCE")) {
			//advances the queue
			advanceQueue();
			send("RESP_SUCCESS");
		}
		if (message.equals("QUEUE_NEXT")) {
			//reads the next user in line to use the tool from the queue
			waitUntilAccessible();
			accessing = true;
			String next = queue.peek();
			accessing = false;
			if (next == null) {
				send("RESP;NONE");
			} else {
				send("RESP;"+xor(next));
			}
		}
		if (message.equals("QUEUE_READ")) {
			//reads the queue back to the client
			String[] content = getQueueContents();
			if (content == null) {
				send("RESP;QUEUE_EMPTY");
			} else {
				String tosend = "RESP";
				for (int i = 0; i < content.length; i++) {
					tosend = tosend + ";" + xor(content[i]);
				}
				send(tosend);
			}
		}
		super.onRecv(message, args);
	}
}

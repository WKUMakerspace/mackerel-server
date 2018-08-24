package edu.wku.makerspace.mackerel.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class Node extends Thread {
	private String nid;
	protected Socket sock;
	protected BufferedReader in;
	protected PrintWriter out;
	private boolean keepRunning = true;
	private boolean requestedClose = false;
	public static long key;
	
	public Node(Socket newsock, String newnid, BufferedReader newin, PrintWriter newout) {
		nid = newnid;
		sock = newsock;
		in = newin;
		out = newout;
		//try {
		//	in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		//	out = new PrintWriter(sock.getOutputStream());
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}
	}
	
	/**
	 * Returns node id of this node.
	 * @return
	 */
	public String getNodeId() {
		return nid;
	}
	
	/**
	 * Xors the input string with a common key. Increases security by preventing the sending of
	 * student ids over unencrypted TCP.
	 * @param input
	 * @return
	 */
	public static String xor(String input) {
		long a = Long.parseLong(input);
		System.out.println(input + " --> " + ((a ^ key) & Integer.MAX_VALUE));
		return "" + ((a ^ key) & Integer.MAX_VALUE);
	}
	
	/**
	 * Sends a message to the node.
	 * @param message
	 */
	public void send(String message) {
		System.out.println("Message sent to node " + nid + ": " + message);
		out.println(message);
		out.flush();
	}
	
	/**
	 * Waits for a line of text to be received from the node and returns it.
	 * @return
	 */
	public String receive() {
		try {
			String line = in.readLine();
			System.out.println("Message received from node " + nid + ": " + line);
			if (line != null) return (line.replace("\n", "").replace("\r", ""));
		} catch (SocketException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Processes a received message and its arguments.
	 * @param message
	 * @param args
	 */
	protected void onRecv(String message, String[] args) {
		if (message.equals("HELLO")) {
			send("HELLO");
		}
		if (message.equals("DISCONNECT")) {
			close();
		}
		if (message.equals("USER_CHECK")) {
			String[] check = DBConn.checkUser(xor(args[0]));
			if (check != null) {
				send("RESP;" + check[0] + ";" + check[1] + ";" + check[2]);
			} else {
				send("RESP_FAILURE");
			}
		}
	}

	@Override
	public void run() {
		send("CONN_SUCCESS");
		while (keepRunning) {
			//listen for input from node
			String line = receive();
			if (line == null) return;
			//System.out.println("Received from node "+nid+": "+line);
			int sep = line.indexOf(";");
			String message = line;
			String[] args = null;
			if (sep != -1) {
				message = line.substring(0, sep);
				args = line.substring(sep+1).split(";");
			}
			onRecv(message, args);
		}
		System.out.println("End of loop for node " + nid + "!");
		int i = NodeServer.indexOfNodeId(nid);
		if (i != -1) {
			NodeServer.removeNode(i);
		}
		System.out.println("Thread for node " + nid + " has finished.");
	}
	
	public boolean isRunning() {
		return keepRunning && !requestedClose;
	}
	
	/**
	 * Forces a node to close connection.
	 */
	public void req_close() {
		System.out.println("Requesting node disconnect for "+nid);
		send("DISCONNECT");
		//close();
	}
	
	/**
	 * Closes the connection to the node.
	 */
	public void close() {
		keepRunning = false;
		try {
			out.close();
			in.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

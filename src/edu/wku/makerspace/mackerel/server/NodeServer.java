package edu.wku.makerspace.mackerel.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class NodeServer {
	private static ServerSocket serv;
	private static Node[] nodes;
	private static Thread acceptThread;
	
	private static BufferedReader temp_in;
	private static PrintWriter temp_out;
	
	private static boolean tryingToClose = false;
	
	/**
	 * Looks for a node with the specified id. Returns null if not found.
	 * @param id
	 * @return
	 */
	public static Node getNodeById(String id) {
		for (Node n : nodes) {
			if (n != null) {
				if (n.getNodeId().equals(id)) {
					return n;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the index of the first node with the given id, or -1 if not found.
	 * @param id
	 * @return
	 */
	public static int indexOfNodeId(String id) {
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				if (nodes[i].getNodeId().equals(id)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Returns the current list of nodes
	 * @return
	 */
	public static Node[] getNodeList() {
		return nodes;
	}
	
	/**
	 * Finds the first empty index in the nodes array, or -1 if none open.
	 * @return
	 */
	private static int getNextEmptyIndex() {
		if (nodes != null) {
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i] == null) return i;
			}
		}
		return -1;
	}
	
	/**
	 * Begins the node server on a specified port.
	 * @param port
	 */
	public static void begin(int port) {
		nodes = new Node[Integer.parseInt(ConfigReader.getOption("max_nodes"))];
		try {
			System.out.println("Starting node server on port " + port);
			serv = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to start node server. Aborting!");
			System.exit(0);
		}
		
		acceptThread = new Thread() {
		    public void run() {
				//loop to accept new node connections
				while (!tryingToClose) {
					Socket sock = null;
					int index = getNextEmptyIndex();
					if (index != -1) {
						try {
							//wait for incoming connection and create a node
							Node node = null;
							//System.out.println("Waiting for new socket attempt...");
							sock = serv.accept();
							System.out.println("Socket created with "+sock.getInetAddress());
							temp_in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
							temp_out = new PrintWriter(sock.getOutputStream());
							
							//gather information about a node before allocating resources
							temp_out.println("WHOIS");
							temp_out.flush();
							String[] info = temp_in.readLine().replace("\n", "").replace("\r", "").split(";");
							info[0] = info[0].replaceAll(" ", "_");
							
							//set node subclass based on type (info[1])
							if (getNodeById(info[0]) == null) {
								switch (info[1]) {
								case "kiosk":
									node = new KioskNode(sock, info[0], temp_in, temp_out);
									break;
								case "admin":
									node = new AdminNode(sock, info[0], temp_in, temp_out);
									break;
								case "tool":
									node = new ToolNode(sock, info[0], temp_in, temp_out);
									break;
								default:
									temp_out.println("CONN_FAILURE;INVALID_TYPE");
									temp_out.flush();
									break;
								}
							} else {
								temp_out.println("CONN_FAILURE;ID_EXISTS");
								temp_out.flush();
							}
							
							//add and start node
							if (node != null) {
								nodes[index] = node;
								System.out.println("Node '"+info[0]+"' of type '"+info[1]+"' created at "+sock.getInetAddress());
								node.start();
							} else {
								if (temp_out != null) temp_out.close();
								if (temp_in != null) temp_in.close();
								if (sock != null) sock.close();
							}
						} catch (SocketException e) {
							if (!tryingToClose) e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
							try {
								if (temp_out != null) temp_out.close();
								if (temp_in != null) temp_in.close();
								if (sock != null) sock.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						temp_in = null;
						temp_out = null;
					}
				}
				tryingToClose = false;
		    }
		};
		acceptThread.start();
	}
	
	public static void removeNode(int index) {
		if (nodes[index] != null) {
			nodes[index] = null;
		}
	}
	
	/**
	 * Closes (or attempts to close) all running threads associated with the NodeServer.
	 * Also releases as many resources as possible.
	 */
	public synchronized static void close() {
		tryingToClose = true;
		
		//close all nodes
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].req_close();
				nodes[i] = null;
			}
		}
		nodes = null;
		
		//stop accepting new nodes
		try {
			if (serv != null) serv.close();
			serv = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

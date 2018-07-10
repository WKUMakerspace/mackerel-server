package edu.wku.makerspace.mackerel.server;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;
import java.util.Scanner;

public class Start {
	public static final String version = "0.2.2";
	private static Scanner input;
	
	public static void main(String[] args) {
		System.out.println("=== Mackerel Server Starting...");
		
		//load configuration file
		try {
			int numopts = ConfigReader.loadFile("properties.conf");
			System.out.println("success (read " + numopts + " options)");
		} catch (IOException e) {
			System.out.println("Failed to load configuration file");
			System.out.println("Aborting!");
			e.printStackTrace();
			System.exit(0);
		}
		
		//generate key for secure communication between nodes
		Node.key = genKey();
		//System.out.println("key: "+Node.key);
		
		//connect to database
		boolean con = DBConn.connect(ConfigReader.getOption("db_user"), ConfigReader.getOption("db_address"), ConfigReader.getOption("db_passwd"));
		if (!con) {
			System.out.println("Failed to connect to MySQL database at " + ConfigReader.getOption("db_address"));
			System.out.println("Aborting!");
			System.exit(0);
		}
		DBConn.query("USE " + ConfigReader.getOption("db_database"));
		System.out.println("Database connection established");
		
		//instantiate appointment calendar
		Calendar.reload();
		
		//begin node server
		System.out.println("Starting node server...");
		NodeServer.begin(Integer.parseInt(ConfigReader.getOption("port")));
		
		//begin command line
		input = new Scanner(System.in);
		System.out.println("=== Mackerel Server v"+version+". Type 'help' for a list of commands.");
		while (runCmdline()) {}
		
		//wrap up threads and close server
		NodeServer.close();
		DBConn.disconnect();
		
		System.out.println("Goodbye!");
	}
	
	private static boolean runCmdline() {
		System.out.print("> ");
		String line = input.nextLine();
		if (line.startsWith("exit")) return false;
		if (line.startsWith("help")) printHelp();
		if (line.startsWith("query ")) DBConn.query(line.substring(6));
		if (line.startsWith("nodes")) {
			Node[] l = NodeServer.getNodeList();
			int active = 0;
			String cmdout = "";
			for (int i = 0; i < l.length; i++) {
				if (l[i] != null) {
					active++;
					cmdout += "  " + i + ": " + l[i].getNodeId() + "\n";
				}
			}
			System.out.println("Current active nodes: " + active);
			System.out.print(cmdout);
		}
		if (line.startsWith("send ")) {
			String[] args = line.substring(5).split(" ");
			String tosend = args[1];
			for (int i = 2; i < args.length; i++) {
				tosend = tosend + " " + args[i];
			}
			Node n = NodeServer.getNodeById(args[0]);
			if (n != null) {
				System.out.println("Sending \'" + tosend + "\' to node \'" + args[0] + "\'");
				n.send(tosend);
			} else {
				System.out.println("Node \'" + args[0] + "\' does not exist!");
			}
		}
		if (line.startsWith("disc ")) {
			String id = line.substring(5);
			Node n = NodeServer.getNodeById(id);
			if (n != null) {
				n.req_close();
			} else {
				System.out.println("Node \'" + id + "\' does not exist!");
			}
		}
		return true;
	}
	
	private static void printHelp() {
		String help = "No one can help you now.";
		System.out.println(help);
	}
	
	private static int genKey() {
		long t = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
		Random rng = new Random(t);
		int salt = 0;
		for (int i = 0; i < Integer.parseInt(ConfigReader.getOption("nth_key")); i++) {
			salt = rng.nextInt();
		}
        return salt;
	}
}

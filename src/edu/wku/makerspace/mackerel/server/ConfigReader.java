package edu.wku.makerspace.mackerel.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class ConfigReader {
	private static List<String[]> data;
	
	/**
	 * Finds an option with the specified name and returns the option value. Returns null if none.
	 * @param name
	 * @return
	 */
	public static String getOption(String name) {
		for (String[] entry : data) {
			if (entry[0].equals(name)) {
				return entry[1];
			}
		}
		return null;
	}
	
	/**
	 * Loads a configuration file from the specified filename. Returns the number of options loaded, 
	 * or -1 if it fails to read the file.
	 * @param filename
	 * @return
	 * @throws IOException 
	 */
	public static int loadFile(String filename) throws IOException {
		System.out.print("Loading config file: " + filename + " ... ");
		data = new Vector<String[]>();
		File f = new File(filename);
		BufferedReader buf = new BufferedReader(new FileReader(f));
		//process each line into distinct variables and values
		for (String line; (line = buf.readLine()) != null;) {
			int sep = line.indexOf(':');
			if (!line.startsWith("#") && sep > 0) {
				String[] entry = new String[2];
				entry[0] = line.substring(0, sep);
				entry[1] = line.substring(sep + 1);
				data.add(entry);
			}
		}
		buf.close();
		return data.size();
	}
}

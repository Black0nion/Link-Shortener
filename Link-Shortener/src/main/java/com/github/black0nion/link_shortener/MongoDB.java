package com.github.black0nion.link_shortener;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

public class MongoDB {
	private static Properties properties;
	
	static {
		try {
			properties = new Properties();
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream("mongo.properties"));
			properties.load(stream);
			stream.close();
			
			if (!(properties.containsKey("ip") && properties.containsKey("port") && properties.containsKey("dbname") && properties.containsKey("dbcollectionname") && properties.containsKey("authdb") && properties.containsKey("username") && properties.containsKey("password") && properties.containsKey("timeout"))) {
				System.out.println("mongo.properties File not correctly filled!");
				System.exit(0);
			}
		} catch (Exception e) {
			if (e instanceof FileNotFoundException)
				System.out.println("File mongo.properties not found! Please refer to the GitHub Repo README for more information!");
			else  {
				e.printStackTrace();
				System.out.println("An error occured while reading the MongoDB Config, terminating!");
			}
			System.exit(-1);
		}
	}
	
	public static final String mongoIP = properties.getProperty("ip");
	public static final String mongoPort = properties.getProperty("port");
	public static final String mongoDBName = properties.getProperty("dbname");
	public static final String mongoDBCollectionName = properties.getProperty("dbcollectionname");
	public static final String mongoAuthDB = properties.getProperty("authdb");
	public static final String mongoUsername = properties.getProperty("username");
	public static final String mongoPassword = properties.getProperty("password");
	public static final String mongoTimeout = properties.getProperty("timeout");
}

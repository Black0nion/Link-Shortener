package com.github.black0nion.link_shortener;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

public class LinkShortener {
	
	private static Properties properties;
	
	static {
		try {
			properties = new Properties();
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream("config.properties"));
			properties.load(stream);
			stream.close();
			
			if (!(properties.containsKey("port"))) {
				System.out.println("config.properties File not correctly filled!");
				System.exit(0);
			}
		} catch (Exception e) {
			if (e instanceof FileNotFoundException)
				System.out.println("File config.properties not found! Please refer to the GitHub Repo README for more information!");
			else  {
				e.printStackTrace();
				System.out.println("An error occured while reading the Config, terminating!");
			}
			System.exit(-1);
		}
	}

	private static int PORT = Integer.parseInt(properties.getProperty("port"));
	
	public static HashMap<String, String> urls;
	
	public static HashMap<String, String> passwords;
	
	private static Random random;
	
	private static void reload() {
		urls = new HashMap<>();
		passwords = new HashMap<>();
		MongoWrapper.init();
	}
	
	public static void setup() {
		int timeout = 30000;
		try {
			timeout = Integer.parseInt(MongoDB.mongoTimeout);
		} catch (Exception e) {
			System.err.println("Timeout is not an int!");
			System.exit(-1);
		}
		
		if (!MongoManager.connect(MongoDB.mongoIP, MongoDB.mongoPort, MongoDB.mongoAuthDB, MongoDB.mongoUsername, MongoDB.mongoPassword, timeout)) {
			System.err.println("Couldn't connect to MongoDB!");
			System.exit(-1);
		}
			
		reload();
		
		Spark.port(PORT);
		
		Spark.internalServerError((request, response) -> {
			response.status(500);
			return "";
		});
		
		Spark.notFound((request, response) -> {
			response.status(404);
			return "";
		});
	}

	public static void main(String[] launchArgs) {
		setup();
		
		CreateLink.init();
		UpdateLink.init();
		DeleteLink.init();
		
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		
		Spark.before(new Filter() {
			@Override
			public void handle(Request request, Response response) {
				response.header("Access-Control-Allow-Origin", "*");
				response.header("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
				response.header("Access-Control-Allow-Headers", "Origin, Content-Type, X-Auth-Token");
				System.out.println("New Request from IP " + (request.headers("X-Real-IP") != null ? request.headers("X-Real-IP") : request.ip()) + " to URL " + request.pathInfo());
			}
		});
		
		while (true) {
			sc.hasNext();
			String line = sc.nextLine();
			final String[] args = line.split(" ");
			if (line.equalsIgnoreCase("reload") || line.equalsIgnoreCase("rl") || line.equalsIgnoreCase("r")) {
				reload();
				System.out.println("Reloaded.");
			} else if (line.equalsIgnoreCase("list") || line.equalsIgnoreCase("l")) {
				System.out.println("All links:");
				for (Map.Entry<String, String> entry : urls.entrySet()) {
					if (passwords.containsKey(entry.getKey()))
						System.out.println(entry.getKey() + " -> " + entry.getValue() + " | " + passwords.get(entry.getKey()));
					else
						System.out.println("NO PASSWORD FOUND: " + entry.getValue());
				}
			} else {
				if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete")) {
					if (args.length < 2) {
						System.out.println("No arg given!");
					} else {
						String url = args[1];
						MongoWrapper.deleteLink(url);
						System.out.println("Deleted Link " + args[1]);
					}
				}
			}
		}
	}
	
	public static String getRandomString() {
		int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    
	    int targetStringLength = 10;
	    if (random == null)
	    	random = new Random();

	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(targetStringLength + 2)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();

	    return generatedString;
	}
	
	public static String getRandomString(int maxLength) {
		int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    
	    if (random == null)
	    	random = new Random();
	    
	    int targetStringLength = random.nextInt(maxLength - 10) + 10;

	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(targetStringLength + 2)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();

	    return generatedString;
	}
}

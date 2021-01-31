package com.github.black0nion.link_shortener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import spark.Spark;

public class LinkShortener {

private static int PORT = 1010;
	
	public static HashMap<String, String> urls;
	
	public static HashMap<String, String> passwords;
	
	private static Random random;
	
	private static void reload() {
		urls = new HashMap<>();
		passwords = new HashMap<>();
	}
	
	public static void setup() {
		if (!MongoManager.connect(MongoDB.mongoIP, MongoDB.port, MongoDB.mongoAuthDB, MongoDB.mongoUsername, MongoDB.mongoPassword)) {
			System.err.println("Couldn't connect to MongoDB!");
			System.exit(-1);
		}
			
		MongoWrapper.init();
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
	
	public static void main(String[] args) {
		setup();
		
		CreateLink.init();
		UpdateLink.init();
		DeleteLink.init();
		
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		
		while (true) {
			sc.hasNext();
			String line = sc.nextLine();
			if (line.equalsIgnoreCase("reload") || line.equalsIgnoreCase("rl") || line.equalsIgnoreCase("r")) {
				reload();
				System.out.println("Reloaded.");
			} else if (line.equalsIgnoreCase("list") || line.equalsIgnoreCase("l")) {
				for (Map.Entry<String, String> entry : urls.entrySet()) {
					if (passwords.containsKey(entry.getKey()))
						System.out.println(entry.getKey() + " -> " + entry.getValue() + " | " + passwords.get(entry.getKey()));
					else
						System.out.println("NO PASSWORD FOUND: " + entry.getValue());
				}
			} else if (line.split(" ")[0].equalsIgnoreCase("del") || line.split(" ")[0].equalsIgnoreCase("delete")) {
				if (line.split(" ").length < 2) {
					System.out.println("No arg given!");
				} else {
					String url = line.split(" ")[1];
					MongoWrapper.deleteLink(url);
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

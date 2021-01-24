package com.github.black0nion.link_shortener;

import static spark.Spark.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.io.Files;

import spark.Spark;

public class GetNewLink {
	
	private static int PORT = 1010;
	
	private static HashMap<String, String> urls;
	private static JSONObject urlsJSON;
	
	private static HashMap<String, String> passwords;
	private static JSONObject passwordsJSON;
	
	private static Random random;
	
	private static File urlsFile;
	private static File passwordsFile;
	
	private static void reload() {
		urlsFile = new File("files/paths.json");
		passwordsFile = new File("files/passwords.json");
		
		try {
			urlsFile.getParentFile().mkdirs();
			urlsFile.createNewFile();
			urlsJSON = new JSONObject(String.join("\n", Files.readLines(urlsFile, StandardCharsets.UTF_8)));
			if (urls == null)
				urls = new HashMap<>();
			else
				urls.clear();
			for (String line : urlsJSON.keySet()) {
				urls.put(line, urlsJSON.getString(line));
			}
		} catch (Exception e) {
			if (!(e instanceof JSONException))
				e.printStackTrace();
			else {
				try {
					urlsJSON = new JSONObject();
					Files.asCharSink(urlsFile, StandardCharsets.UTF_8).write(urlsJSON.toString(1));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		try {
			urlsFile.getParentFile().mkdirs();
			passwordsFile.createNewFile();
			passwordsJSON = new JSONObject(String.join("\n", Files.readLines(passwordsFile, StandardCharsets.UTF_8)));
			if (passwords == null)
				passwords = new HashMap<>();
			else
				passwords.clear();
			for (String line : passwordsJSON.keySet()) {
				passwords.put(line, passwordsJSON.getString(line));
			}
		} catch (Exception e) {
			if (!(e instanceof JSONException))
				e.printStackTrace();
			else {
				try {
					passwordsJSON = new JSONObject();
					Files.asCharSink(passwordsFile, StandardCharsets.UTF_8).write(passwordsJSON.toString(1));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static void setup() {
		reload();
			
		Spark.port(PORT);
		
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		
		Spark.internalServerError((request, response) -> {
			response.status(500);
			return "";
		});
		
		Spark.notFound((request, response) -> {
			response.status(404);
			return "";
		});
		
		post("/getlink", (request, response) -> {
			String url = request.headers("url");
			final String redirect_url = request.headers("redirect_url");
			
			if (redirect_url == null) {
				response.status(400);
				return "";
			}
			
			if (url == null) {
				String randomUrl;
				do {
					randomUrl = getRandomString();
				} while (urls.containsKey(randomUrl));
				url = randomUrl;
			}
			
			String password = (request.headers("password") != null ? request.headers("password") : getRandomString(20));
			
			System.out.println("New URL made by IP " + request.ip() + " with password " + password + ": " + url + " -> " + redirect_url);
			
			if (urls.containsKey(url)) {
				response.status(409);
				return "";
			}
			
			urls.put(url, redirect_url);
			passwords.put(url, password);
			
			try {
				urlsJSON.put(url, redirect_url);
				passwordsJSON.put(url, password);
				Files.asCharSink(urlsFile, StandardCharsets.UTF_8).write(urlsJSON.toString(1));
				Files.asCharSink(passwordsFile, StandardCharsets.UTF_8).write(passwordsJSON.toString(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			get(url, (req, res) -> {
				res.redirect(redirect_url);
				System.out.println("Redirected a user (IP " + req.ip() + ")! " + req.url() + " -> " + redirect_url);
				return null;
			});
			
			response.status(200);
			return new JSONObject().put("password", password);
		});
		
		delete("/delete", (request, response) -> {
			final String password = request.headers("password");
			final String url = request.headers("url");
			
			if (password == null || url == null) {
				response.status(400);
				return "";
			}
			
			if (!urls.containsKey(url)) {
				response.status(400);
				return "";
			}
			
			
			if (!passwords.get(url).equals(password)) {
				response.status(403);
				return "";
			}
			
			// everything fine
			urls.remove(url);
			passwords.remove(url);
			
			unmap(url);
			
			try {
				urlsJSON.remove(url);
				passwordsJSON.remove(url);
				Files.asCharSink(urlsFile, StandardCharsets.UTF_8).write(urlsJSON.toString(1));
				Files.asCharSink(passwordsFile, StandardCharsets.UTF_8).write(passwordsJSON.toString(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return "";
		});
		
		patch("/update", (request, response) -> {
			final String password = request.headers("password");
			final String url = request.headers("url");
			final String newUrl = request.headers("new_url");
			
			if (password == null || url == null || newUrl == null) {
				response.status(400);
				return "";
			}
			
			if (!urls.containsKey(url)) {
				response.status(400);
				return "";
			}
			
			if (!passwords.get(url).equals(password)) {
				response.status(403);
				return "";
			}
			
			String redirectURL = urls.get(url);
			
			unmap(url);
			get(newUrl, (req, res) -> {
				res.redirect(redirectURL);
				System.out.println("Redirected a user (IP " + req.ip() + ")! " + req.url() + " -> " + redirectURL);
				return null;
			});
			
			try {
				urls.remove(url);
				urls.put(newUrl, redirectURL);
				passwords.remove(url);
				passwords.put(newUrl, password);
				urlsJSON.remove(url);
				urlsJSON.put(newUrl, redirectURL);
				passwordsJSON.remove(url);
				passwordsJSON.put(newUrl, password);
				Files.asCharSink(urlsFile, StandardCharsets.UTF_8).write(urlsJSON.toString(1));
				Files.asCharSink(passwordsFile, StandardCharsets.UTF_8).write(passwordsJSON.toString(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return "";
		});
		
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
					urls.remove(url);
					passwords.remove(url);
					
					unmap(url);
					
					try {
						urlsJSON.remove(url);
						passwordsJSON.remove(url);
						Files.asCharSink(urlsFile, StandardCharsets.UTF_8).write(urlsJSON.toString(1));
						Files.asCharSink(passwordsFile, StandardCharsets.UTF_8).write(passwordsJSON.toString(1));
					} catch (Exception e) {
						e.printStackTrace();
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

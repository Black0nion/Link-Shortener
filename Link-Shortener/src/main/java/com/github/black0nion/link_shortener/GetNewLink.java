package com.github.black0nion.link_shortener;

import static spark.Spark.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.io.Files;

import spark.Spark;

public class GetNewLink {
	
	private static HashMap<String, String> urls = new HashMap<>();
	private static JSONObject urlsJSON;
	
	private static HashMap<String, String> passwords = new HashMap<>();
	private static JSONObject passwordsJSON;
	
	private static Random random;
	
	public static void setup() {
		
		File urlsFile = new File("files/paths.json");
		File passwordsFile = new File("files/passwords.json");
		
		try {
			urlsFile.getParentFile().mkdirs();
			urlsFile.createNewFile();
			urlsJSON = new JSONObject(String.join("\n", Files.readLines(urlsFile, StandardCharsets.UTF_8)));
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
			
		Spark.port(1010);
		
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
				response.status(404);
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
	}
	
	public static String getRandomString() {
		int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    
	    int targetStringLength = 10;
	    if (random == null)
	    	random = new Random();
	    
	    //int targetStringLength = random.nextInt(18);

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
	    
	    int targetStringLength = random.nextInt(18);

	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(targetStringLength + 2)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();

	    return generatedString;
	}
}

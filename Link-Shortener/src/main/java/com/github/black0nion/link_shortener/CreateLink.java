package com.github.black0nion.link_shortener;

import static spark.Spark.get;
import static spark.Spark.post;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.google.common.io.Files;

public class CreateLink {
	
	public static void init() {
		
		post("/getlink", (request, response) -> {
			String url = request.headers("url");
			final String redirectUrl = request.headers("redirect_url");
			
			if (redirectUrl == null) {
				response.status(400);
				return "";
			}
			
			if (url == null) {
				String randomUrl;
				do {
					randomUrl = LinkShortener.getRandomString();
				} while (LinkShortener.urls.containsKey(randomUrl));
				url = randomUrl;
			}
			
			String password = (request.headers("password") != null ? request.headers("password") : LinkShortener.getRandomString(20));
			
			System.out.println("New URL made by IP " + request.ip() + " with password " + password + ": " + url + " -> " + redirectUrl);
			
			if (LinkShortener.urls.containsKey(url)) {
				response.status(409);
				return "";
			}
			
			createLink(url, redirectUrl, password);
			
			response.status(200);
			return new JSONObject().put("password", password);
		});
	}
	
	public static void createLink(String url, String redirectUrl, String password) {
		LinkShortener.urls.put(url, redirectUrl);
		LinkShortener.passwords.put(url, password);
		
		try {
			LinkShortener.urlsJSON.put(url, redirectUrl);
			LinkShortener.passwordsJSON.put(url, password);
			Files.asCharSink(LinkShortener.urlsFile, StandardCharsets.UTF_8).write(LinkShortener.urlsJSON.toString(1));
			Files.asCharSink(LinkShortener.passwordsFile, StandardCharsets.UTF_8).write(LinkShortener.passwordsJSON.toString(1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		get(url, (req, res) -> {
			res.redirect(redirectUrl);
			System.out.println("Redirected a user (IP " + req.ip() + ")! " + req.url() + " -> " + redirectUrl);
			return null;
		});
	}
}

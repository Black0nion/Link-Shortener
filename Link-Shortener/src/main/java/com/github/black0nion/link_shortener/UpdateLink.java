package com.github.black0nion.link_shortener;

import static spark.Spark.get;
import static spark.Spark.patch;
import static spark.Spark.unmap;

import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;

public class UpdateLink {
	public static void init() {
		patch("/update", (request, response) -> {
			final String password = request.headers("password");
			final String url = request.headers("url");
			final String newUrl = request.headers("new_url");
			
			if (password == null || url == null || newUrl == null) {
				response.status(400);
				return "";
			}
			
			if (!LinkShortener.urls.containsKey(url)) {
				response.status(400);
				return "";
			}
			
			if (!LinkShortener.passwords.get(url).equals(password)) {
				response.status(403);
				return "";
			}
			
			String redirectURL = LinkShortener.urls.get(url);
			
			get(newUrl, (req, res) -> {
				res.redirect(redirectURL);
				System.out.println("Redirected a user (IP " + req.ip() + ")! " + req.url() + " -> " + redirectURL);
				return null;
			});
			
			editLink(url, newUrl, redirectURL, password);
			
			return "";
		});
	}
	

	
	public static void editLink(String url, String newUrl, String redirectURL, String password) {
		try {
			unmap(url);
			LinkShortener.urls.remove(url);
			LinkShortener.urls.put(newUrl, redirectURL);
			LinkShortener.passwords.remove(url);
			LinkShortener.passwords.put(newUrl, password);
			LinkShortener.urlsJSON.remove(url);
			LinkShortener.urlsJSON.put(newUrl, redirectURL);
			LinkShortener.passwordsJSON.remove(url);
			LinkShortener.passwordsJSON.put(newUrl, password);
			Files.asCharSink(LinkShortener.urlsFile, StandardCharsets.UTF_8).write(LinkShortener.urlsJSON.toString(1));
			Files.asCharSink(LinkShortener.passwordsFile, StandardCharsets.UTF_8).write(LinkShortener.passwordsJSON.toString(1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

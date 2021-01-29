package com.github.black0nion.link_shortener;

import static spark.Spark.get;
import static spark.Spark.patch;

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
			
			MongoWrapper.editLink(url, newUrl, redirectURL, password);
			
			return "";
		});
	}
}

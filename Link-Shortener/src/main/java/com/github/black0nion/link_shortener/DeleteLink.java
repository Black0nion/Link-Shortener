package com.github.black0nion.link_shortener;

import static spark.Spark.delete;

public class DeleteLink {
	public static void init() {

		delete("/delete", (request, response) -> {
			final String password = request.headers("password");
			final String url = request.headers("url");
			
			if (password == null || url == null) {
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
			
			System.out.println("Deleted Link " + url + " redirecting to " + LinkShortener.urls.get(url) + " by IP " + (request.headers("X-Real-IP") != null ? request.headers("X-Real_IP") : request.ip()));
			
			MongoWrapper.deleteLink(url);
			
			return "";
		});
	}
}

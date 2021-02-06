package com.github.black0nion.link_shortener;

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
			
			System.out.println("Edited Link " + url + " from " + LinkShortener.urls.get(url) + " to " + newUrl + " by IP " + (request.headers("X-Real-IP") != null ? request.headers("X-Real-IP") : request.ip()));
			MongoWrapper.editLink(url, newUrl, password);
			
			return "";
		});
	}
}

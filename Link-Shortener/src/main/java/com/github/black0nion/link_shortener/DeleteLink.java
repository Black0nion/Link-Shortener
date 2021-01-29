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
			
			MongoWrapper.deleteLink(url);
			
			return "";
		});
	}
}

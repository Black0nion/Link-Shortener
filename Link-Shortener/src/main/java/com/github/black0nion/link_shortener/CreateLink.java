package com.github.black0nion.link_shortener;

import static spark.Spark.post;

import org.json.JSONObject;

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
			
			MongoWrapper.createLink(url, redirectUrl, password);
			
			response.status(200);
			return new JSONObject().put("password", password);
		});
	}
}

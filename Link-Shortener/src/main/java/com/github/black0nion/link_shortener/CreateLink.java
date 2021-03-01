package com.github.black0nion.link_shortener;

import static spark.Spark.post;

import org.json.JSONObject;

public class CreateLink {
	
	public static void init() {
		
		post("/getlink", (request, response) -> {
			String url = request.headers("url");
			final String redirectUrl = request.headers("redirecturl");
			
			if (redirectUrl == null) {
				response.status(400);
				return "400 BAD REQUEST";
			}
			
			if (url == null) {
				String randomUrl;
				do {
					randomUrl = LinkShortener.getRandomString();
				} while (LinkShortener.urls.containsKey(randomUrl));
				url = randomUrl;
			}

			if (MongoWrapper.linkExisting(url)) {
				response.status(409);
				return "409 CONFLICT";
			}
			
			String password = (request.headers("password") != null ? request.headers("password") : LinkShortener.getRandomString(20));
			
			System.out.println("Created Link " + url + " redirecting to " + redirectUrl + " with password " + password + " by IP " + (request.headers("X-Real-IP") != null ? request.headers("X-Real-IP") : request.ip()));
			
			MongoWrapper.createLink(url, redirectUrl, password);
			
			response.header("Crossorigin", "anonymous");
			response.header("Access-Control-Allow-Origin", "*");
			response.status(200);
			return new JSONObject().put("password", password);
		});
	}
}

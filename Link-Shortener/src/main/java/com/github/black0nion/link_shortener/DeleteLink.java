package com.github.black0nion.link_shortener;

import static spark.Spark.delete;
import static spark.Spark.unmap;

import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;

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
			
			deleteLink(url);
			
			return "";
		});
	}
	

	
	public static void deleteLink(String url) {
		LinkShortener.urls.remove(url);
		LinkShortener.passwords.remove(url);
		
		unmap(url);
		
		try {
			LinkShortener.urlsJSON.remove(url);
			LinkShortener.passwordsJSON.remove(url);
			Files.asCharSink(LinkShortener.urlsFile, StandardCharsets.UTF_8).write(LinkShortener.urlsJSON.toString(1));
			Files.asCharSink(LinkShortener.passwordsFile, StandardCharsets.UTF_8).write(LinkShortener.passwordsJSON.toString(1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

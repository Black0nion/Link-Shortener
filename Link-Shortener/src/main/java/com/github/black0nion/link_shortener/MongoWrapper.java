package com.github.black0nion.link_shortener;

import static spark.Spark.get;
import static spark.Spark.unmap;

import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;

public class MongoWrapper {

	
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

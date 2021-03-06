package com.github.black0nion.link_shortener;

import static spark.Spark.get;
import static spark.Spark.unmap;

import java.util.HashMap;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

public class MongoWrapper {

	private static MongoCollection<Document> collection;
	
	public static void init() {
		collection = MongoManager.getCollection(MongoDB.mongoDBCollectionName, MongoManager.getDatabase(MongoDB.mongoDBName));
		
		if (collection.estimatedDocumentCount() == 0)
			return; 
		
		HashMap<String, String> tempUrls = new HashMap<>();
		HashMap<String, String> tempPasswords = new HashMap<>();
		
		for (Document doc : collection.find()) {
			if (doc.containsKey("url") && doc.containsKey("redirecturl") && doc.containsKey("password")) {
				tempUrls.put(doc.getString("url"), doc.getString("redirecturl"));
				tempPasswords.put(doc.getString("url"), doc.getString("password"));
			}
		}
		
		if (tempUrls.size() == 0 || tempPasswords.size() == 0)
			return;
		
		LinkShortener.urls.putAll(tempUrls);
		LinkShortener.passwords.putAll(tempPasswords);
		tempUrls = null;
		tempPasswords = null;
	}
	
	public static boolean linkExisting(String url) {
		return LinkShortener.urls.containsKey(url);
	}
	
	public static void createLink(String url, String redirectURL, String password) {
		try {
			MongoManager.insertOne(collection, new Document().append("url", url).append("redirecturl", redirectURL).append("password", password));
			LinkShortener.urls.put(url, redirectURL);
			LinkShortener.passwords.put(url, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		get(url, (req, res) -> {
			res.redirect(redirectURL);
			System.out.println("Redirected a user (IP " + (req.headers("X-Real-IP") != null ? req.headers("X-Real-IP") : req.ip()) + ")! " + req.url() + " -> " + redirectURL);
			return null;
		});
	}
	
	public static void editLink(String url, String newUrl, String password) {
		try {
			unmap(url);
			LinkShortener.urls.remove(url);
			LinkShortener.urls.put(url, newUrl);
			LinkShortener.passwords.remove(url);
			LinkShortener.passwords.put(url, password);
			MongoManager.updateOne(collection, new BasicDBObject("url", url), new BasicDBObject("redirecturl", newUrl));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		get(url, (req, res) -> {
			res.redirect(newUrl);
			System.out.println("Redirected a user (IP " + req.ip() + ")! " + req.url() + " -> " + newUrl);
			return null;
		});
	}
	
	public static void deleteLink(String url) {
		unmap(url);
		LinkShortener.urls.remove(url);
		LinkShortener.passwords.remove(url);
		collection.deleteOne(new Document("url", url));
	}
}

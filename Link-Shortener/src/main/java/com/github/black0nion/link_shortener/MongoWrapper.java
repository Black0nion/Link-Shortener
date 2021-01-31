package com.github.black0nion.link_shortener;

import static spark.Spark.get;
import static spark.Spark.unmap;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

public class MongoWrapper {

	private static MongoCollection<Document> collection;
	
	public static void init() {
		collection = MongoManager.getCollection(MongoDB.mongoDBCollectionName, MongoManager.getDatabase(MongoDB.mongoDBName));
	}
	
	public static boolean linkExisting(String url) {
		return MongoManager.getDocumentInCollection(collection, "url", url) != null;
	}
	
	public static void createLink(String url, String redirectURL, String password) {
		try {
			MongoManager.insertOne(collection, new Document().append("url", url).append("redirect_url", redirectURL).append("password", password));
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
			MongoManager.updateOne(collection, new BasicDBObject("url", url), new BasicDBObject("redirect_url", newUrl));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteLink(String url) {
		unmap(url);
		LinkShortener.urls.remove(url);
		LinkShortener.passwords.remove(url);
		collection.deleteOne(new Document("url", url));
	}
}

package com.ppot14.futbol7;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

public class DBConnector {
	private static final Logger logger = Logger.getLogger(DBConnector.class.getName());
	private static final String LOCAL_DB_SERVER = "127.0.0.1";
	private static final String PROD_DB_SERVER = "vps238730.ovh.net";
	private static MongoClient mongo;
	private static String dbServer = PROD_DB_SERVER;

	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

	private static MongoCollection<Document> getCollection(String collectionName) throws Exception {
		if (mongo == null){
			MongoCredential credential = MongoCredential.createCredential("ppot14", "admin", "1468314a".toCharArray());
			mongo = new MongoClient(new ServerAddress(dbServer, 27017), Arrays.asList(credential));
		}
		MongoCollection<Document> collection = mongo.getDatabase("futbol7").getCollection(collectionName);
		return collection;
	}
	
	public static Document getConfig(){
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Config");
			Bson filter = exists("permanents");
			FindIterable<Document> result = configCollection.find(filter);
			if(result!=null && result.first()!=null){
				logger.info(""+(result.first()!=null));
				return (Document) result.first();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Document getPlayer(JsonNode jsonNode){
		String name = jsonNode.get("name").asText();
		String id = jsonNode.get("id").asText();
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Players");
			Bson filter = and(eq("name", name),eq("id",id));
			FindIterable<Document> result = configCollection.find(filter);
			if(result!=null && result.first()!=null){
				Bson update = set("lastAccess", new Date().getTime());
				//Bson update = and(set("picture", jsonNode.get("picture").asText()),set("lastAccess", new Date().getTime()));
				configCollection.updateOne(filter,update);
				logger.fine("player exists: "+result.first().getString("name")+" ("+result.first().getString("nameweb")+")");
				return (Document) result.first();
			}else{
				((ObjectNode)jsonNode).put("created", new Date().getTime());
				ObjectMapper mapper = new ObjectMapper();
				configCollection.insertOne(new Document((Map<String, Object>) mapper.convertValue(jsonNode, Map.class)));
				logger.info("new player: "+jsonNode.get("name").asText());
				return new Document("newuser",true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Map<String,String> getPlayersPictures(){
		Map<String,String> playersPictures = new HashMap<String,String>();
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Players");
			for (Document document : configCollection.find()) {
				playersPictures.put(document.getString("nameweb")!=null?document.getString("nameweb"):document.getString("name"), document.getString("picture"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return playersPictures;
	}

	public static boolean setPlayerPicture(String player, String picture){
		boolean result = false;
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Players");
			UpdateResult updateResult = configCollection.updateMany(eq("nameweb", player),set("picture", picture));
			logger.info("setPlayerPicture "+player+" with "+picture+" to "+updateResult.getModifiedCount()+" records");
			result = updateResult.getModifiedCount()>0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}
	
	public static Document hasVoted(JsonNode jsonNode){
		String name = jsonNode.get("name")!=null?jsonNode.get("name").asText():jsonNode.get("voter").asText();
		String season = jsonNode.get("season").asText();
		Long dateL = jsonNode.get("date").asLong();
		String date = formatter.format(new Date(dateL));
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Scores");
			Bson filter = and(eq("scoresMVP.voter",name),eq("date",date),eq("season",season));
			FindIterable<Document> result = configCollection.find(filter);
			if(result!=null && result.first()!=null){
				logger.info("player voted: "+jsonNode);
				return (Document) result.first();
			}else{
				logger.info("player didn't vote: "+jsonNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Document getVotes(String season){
		Document ret = new Document();
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Scores");
			Bson filter = eq("season",season);
			FindIterable<Document> result = configCollection.find(filter);
			if(result!=null){
				for(Document d:result){
					ret.put(d.getString("date"), d);
				}
				return ret;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Document getVotes(String season, String date){
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Scores");
			Bson filter = and(eq("season",season), eq("date",date));
			FindIterable<Document> result = configCollection.find(filter);
			if(result!=null && result.first()!=null){
				return (Document) result.first();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void createScore(Document data){
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Scores");
			configCollection.insertOne(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static Boolean addPunctuation(String voter, String voted, Long dateL, String season){
		String date = formatter.format(new Date(dateL));
		try {
			MongoCollection<Document> configCollection;
			configCollection = getCollection("Scores");
			Bson filter = and(eq("season",season), eq("date",date));
			Document s = new Document();
			s.put("voter", voter);
			s.put("voted", voted);
			UpdateResult res = configCollection.updateOne(filter, new Document("$push", new Document("scoresMVP", s)));
			if(res.getModifiedCount()==1){ return true; }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}

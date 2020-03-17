package com.ppot14.futbol7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.mortbay.log.Log;

public class APIUtil {
	
	private static final Logger logger = Logger.getLogger(APIUtil.class.getName());

	private static final String TROMPITO = "trompito";
	private static final String DANDY = "dandy";
	private static final String FRANCES = "frances";
	private static final String SILLEGAS = "sillegas";
	private static final String PORCULERO = "porculero";
//	private static final String LOCAL = "local";
//	private static final String PRODUCTION = "production";
	private static double MIN_VALID_MATCHES = 0.25d;
//	private static String ENVIRONMENT = LOCAL;
	
	private static final List<String> TITLES = Arrays.asList(TROMPITO,DANDY,FRANCES,SILLEGAS,PORCULERO);
	
	private static Map<String,List<String>> PERMANENTS = null;
	
	private static final long pollingLimit = 4*24*60*60*1000 + 12*60*60*1000;//4 days and half. Friday at midday or Sunday and middays

	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
	private static SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy");
//	private static SimpleDateFormat formatter3 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private Map<String,List<Map<String,String>>> fullRanking = null;
	private Map<String,List<Map<String,String>>> fullScorers = null;
	private Map<String,Map<String,Map<String,Integer>>> scorersByDate = null;
	private Map<String,Map<String, Integer>> scorersByName = null;
	private Map<String,Integer> numMatches = new HashMap<String, Integer>();
	private Map<String,Integer> numScorers = new HashMap<String, Integer>();
	private Map<String,List<List<String>>> rawMatches = null;
	private Map<String,List<List<String>>> rawScorers = null;
	private Map<String,Set<String>> players = null;	
	
	@SuppressWarnings({ "unchecked" })
	public APIUtil(Map<String,Object> config){
		
		PERMANENTS = (Map<String,List<String>>) config.get("permanents");
		MIN_VALID_MATCHES = (Double) config.get("minimumValidMatches");
//		ENVIRONMENT = (String) config.get("environment");
	}

	public Object getPermanents() {
		Map<String, Object> options = new HashMap<String,Object>();
		options.put("permanents", PERMANENTS);
		return options;
	}

	public Object getPermanents(Map<String,Object> config) {
		Map<String, Object> options = new HashMap<String,Object>();
		options.put("permanents", (Map<String,List<String>>) config.get("permanents"));
		return options;
	}

	@SuppressWarnings("unchecked")
	public synchronized boolean processData(boolean refresh, Map<String,Object> config){
    	
		try{
			if(refresh){
				PERMANENTS = (Map<String,List<String>>) config.get("permanents");
				MIN_VALID_MATCHES = (Double) config.get("minimumValidMatches");
//				ENVIRONMENT = (String) config.get("environment");
			}
			if(rawMatches==null || refresh){
				
				InputStream inputStream = null;
				try{
					inputStream = GoogleImporter.importFromGoogleDrive(config,null);
				}catch(SocketTimeoutException e){
					logger.severe("Error downloading from Google Drive: "+e.getMessage());
					return false;
				}
				rawMatches = formatPOIdata(inputStream);
				rawScorers = new TreeMap<String, List<List<String>>>();
//				matches = formatODSdata(inputStream);
				Iterator<Entry<String, List<List<String>>>> it = rawMatches.entrySet().iterator();
				while(it.hasNext()){
					Entry<String, List<List<String>>> item = it.next();
					String s = item.getKey();
					if(s.contains("Goleadores")){
						rawScorers.put(s, item.getValue());
						it.remove();
						numScorers.put(s, rawScorers.get(s).size()-3);
					}else{
						item.getValue().remove(0);
						Iterator<List<String>> i = item.getValue().iterator();
						while (i.hasNext()) {
						   if(isRowEmpty(i.next())){
							   i.remove();
						   }
						}
						numMatches.put(s, item.getValue().size());
					}
				}

				setScorers();
				setRanking();
				
				return true;
			
			}else{
//				logger.info("Process data skipped, matches already loaded");
			}
        
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * @return the fullRanking
	 */
	public Map<String, String> getUserStats(String season, String user) {
		for(Map<String,String> row : fullRanking.get(season)){
			if(user.equals(row.get("name"))){
				return row;
			}
		}
		return null;
	}
	
	/**
	 * @return the fullRanking
	 */
	public Map<String,List<Map<String, String>>> getFullRanking() {
		return fullRanking;
	}
	public List<Map<String, String>> getFullRanking(String season) {
		return fullRanking.get(season);
	}
	
	/**
	 * @return the fullRanking
	 */
	public Map<String, List<Map<String, String>>> getFullScorers() {
		return fullScorers;
	}
	public List<Map<String, String>> getFullScorers(String season) {
		return fullScorers.get(season);
	}

	public Map<String,List<Map<String, Object>>> getResults() {
		
		Map<String,List<Map<String, Object>>> res = new HashMap<String,List<Map<String,Object>>>();

		try{
			for(Entry<String, List<List<String>>> seasonMatches: rawMatches.entrySet()){
				List<Map<String, Object>> res1 = new ArrayList<Map<String,Object>>();
				for(List<String> match : seasonMatches.getValue()){
					if(isRowEmpty(match)) break;
					Map<String, Object> formattedMatch = new HashMap<String, Object>();
					
					formattedMatch.put("day", formatter.parse(match.get(0)));
					formattedMatch.put("scoreBlues", match.get(8));
					formattedMatch.put("scoreWhites", match.get(9));
					formattedMatch.put("remarks", match.size()>17?match.get(17):"");
					List<Map<String,String>> teams = new ArrayList<Map<String,String>>();
					for(int i=1;i<8;i++){
						Map<String,String> r = new HashMap<String, String>();
						r.put("blue", match.get(i));
						r.put("white", match.get(i+9));
						teams.add(r);
					}
					formattedMatch.put("data", teams);
					res1.add(formattedMatch);
				}
				res.put(seasonMatches.getKey(), res1);
			}
		}catch(Exception e){
			Log.warn("Error getting results: "+e.getMessage());
			e.printStackTrace();
		}
		
		return res;
	}
	public List<Map<String, Object>> getResults(String season) {
		return getResults().get(season);
	}

	public List<Map<String, Object>> getPointsSeries(String season) {
		return getPointsSeries(season, null);
	}
	public List<Map<String, Object>> getPointsSeries(String season, String user) {
		Date today = new Date();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		
		try{
			for(String name : players.get(season)){
				if(user==null || user.equals(name)){
	    			int points = 0;
	            	List<List<Object>> playerData = new ArrayList<List<Object>>();
	            	List<List<Object>> playerData2 = new ArrayList<List<Object>>();
	    			for(List<String> row : rawMatches.get(season)){
		    			if(isRowEmpty(row)) break;
		        		if(row.contains(name)){
			        		int i=0;
			        		int gA=0,gB=0;
			        		String date = null;
			        		String colour=null;
			        		for(String cell : row){
			        			if(i==0){date=cell;}
			        			if(i==8){gA=Math.round(Float.parseFloat(cell));}
			        			if(i==9){gB=Math.round(Float.parseFloat(cell));}
			            		if(cell.equals(name)){
			            			if(i<9){colour="a";}
			            			if(i>8){colour="b";}
			            		}
			            		i++;
			            	}
			        		if(colour!=null){
			        			List<Object> game = new ArrayList<Object>();
			        			Date d = formatter.parse(date);
			        			game.add(d);
								int gFor = ("a".equals(colour)?gA:gB);
								int gAgainst = ("a".equals(colour)?gB:gA);
								int pointsM = ((gFor>gAgainst)?3:((gFor<gAgainst)?0:1));
			        			points += pointsM;
			        			game.add(points);
			        			playerData.add(game);
			        			if(name.equals(user)){
				        			List<Object> score = new ArrayList<Object>();
				        			score.add(d);
				        			score.add( (scorersByDate.get(season)!=null && 
												scorersByDate.get(season).get(date)!=null && 
												scorersByDate.get(season).get(date).get(user)!=null)?
												scorersByDate.get(season).get(date).get(user): 0);
				        			playerData2.add(score);
			        			}
			        		}else{
			        			continue;
			        		}
		        		}
		        		
		        	}
	
	            	//Hack to add current points at current day to the current season
	    			if(user==null && season.contains(Integer.toString(Calendar.getInstance().get(Calendar.YEAR))) ){
	    	        	Integer maxPts = (Integer) playerData.get(playerData.size()-1).get(1);
	    				List<Object> game = new ArrayList<Object>();
	    				game.add(today);
	    				game.add(maxPts);
	    				playerData.add(game);
	    			}
	    			
	            	Map<String, Object> e = new HashMap<String, Object>();
	            	e.put("name", name);
	            	e.put("data", playerData);
	        		if(name.equals(user)){
		            	e.put("name", "Puntos "+name);
		            	Map<String, Object> e2 = new HashMap<String, Object>();
		            	e2.put("name", "Goles");
		            	e2.put("data", playerData2);
		            	e2.put("yAxis", 1);
		            	e2.put("type", "column");
		        		data.add(e2);
	        		}
	        		data.add(e);
				}
    			
    		}
		
		}catch(Exception e){
			Log.warn("Error getting point series: "+e.getMessage()+", season: "+season+", user: "+user);
			e.printStackTrace();
		}
        
		return data;
	}

	public Map<String,List<Map<String,String>>> getVS() {
		Map<String,List<Map<String,String>>> data = new TreeMap<String,List<Map<String,String>>>();

		for(Entry<String, List<List<String>>> seasonMatches: rawMatches.entrySet()){
		
			Map<Set<String>,Integer> vs = new HashMap<Set<String>,Integer>();
			
			for(List<String> row: seasonMatches.getValue()){
				if(isRowEmpty(row)) break;
				for(int i=1;i<8;i++){
					for(int j=10;j<17;j++){
						Set<String> pair = new HashSet<String>(Arrays.asList(row.get(i), row.get(j)));
						vs.put(pair, ((vs.containsKey(pair))?vs.get(pair):0) + 1);
					}
				}
			}
			
			data.put(seasonMatches.getKey(), new ArrayList<Map<String,String>>());
	        for(Entry<Set<String>, Integer> row : vs.entrySet()){
	        	if(row.getValue()>1){
		        	Map<String, String> e = new HashMap<String, String>();
		        	e.put("player1", (String)row.getKey().toArray()[0]);
		        	e.put("player2", (String)row.getKey().toArray()[1]);
		        	e.put("vs", row.getValue().toString());
					data.get(seasonMatches.getKey()).add(e);
	        	}
	        }
		}
		
		return data;
		
	}
	public List<Map<String,String>> getVS(String season) {
		return getVS().get(season);
	}

	public Map<String,List<Map<String,String>>> getPair() {
		Map<String,List<Map<String,String>>> data = new TreeMap<String,List<Map<String,String>>>();

		for(Entry<String, List<List<String>>> seasonMatches: rawMatches.entrySet()){
			Map<Set<String>,Integer> vs = new HashMap<Set<String>,Integer>();
			
			for(List<String> row: seasonMatches.getValue()){
				if(isRowEmpty(row)) break;
				for(int i=1;i<8;i++){
					for(int j=(i+1);j<8;j++){
						Set<String> pair = new HashSet<String>(Arrays.asList(row.get(i), row.get(j)));
						vs.put(pair, ((vs.containsKey(pair))?vs.get(pair):0) + 1);
					}
				}
				for(int i=10;i<17;i++){
					for(int j=(i+1);j<17;j++){
						Set<String> pair = new HashSet<String>(Arrays.asList(row.get(i), row.get(j)));
						vs.put(pair, ((vs.containsKey(pair))?vs.get(pair):0) + 1);
					}
				}
			}

			data.put(seasonMatches.getKey(), new ArrayList<Map<String,String>>());
	        for(Entry<Set<String>, Integer> row : vs.entrySet()){
	        	if(row.getValue()>1){
		        	Map<String, String> e = new HashMap<String, String>();
		        	e.put("player1", (String)row.getKey().toArray()[0]);
		        	e.put("player2", (String)row.getKey().toArray()[1]);
		        	e.put("pair", row.getValue().toString());
					data.get(seasonMatches.getKey()).add(e);
	        	}
	        }
		}
		return data;
		
	}
	public List<Map<String,String>> getPair(String season) {
		return getPair().get(season);
	}
	
	public static String jsonToString(Object o) throws JsonGenerationException, JsonMappingException, IOException{
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(o);
        return jsonInString;
		
	}
	
	public Map<String,List<Map<String,String>>> getRankingPermanents(){

		Map<String,List<Map<String,String>>> permanents = new HashMap<String,List<Map<String,String>>>();
		for(Entry<String, List<Map<String, String>>> season : fullRanking.entrySet()){
			permanents.put(season.getKey(), new ArrayList<Map<String, String>>());
			List<String> seasonPermanents = PERMANENTS.containsKey(season.getKey())?
					PERMANENTS.get(season.getKey()):
					PERMANENTS.get("default");
			for(Map<String, String> row : season.getValue()){
	        	if(seasonPermanents.contains(row.get("name")) ){
	        		permanents.get(season.getKey()).add(row);
	        	}
			}
		}
        return permanents;
        
	}
	public List<Map<String,String>> getRankingPermanents(String season){
		return getRankingPermanents().get(season);
	}
	
	public Map<String,List<Map<String,String>>> getRankingSubstitutes(){
		Map<String,List<Map<String,String>>> substitutes = new HashMap<String,List<Map<String,String>>>();
		for(Entry<String, List<Map<String, String>>> season : fullRanking.entrySet()){
			substitutes.put(season.getKey(), new ArrayList<Map<String, String>>());
			List<String> seasonPermanents = PERMANENTS.containsKey(season.getKey())?
					PERMANENTS.get(season.getKey()):
					PERMANENTS.get("default");
			for(Map<String, String> row : season.getValue()){
	        	if(!seasonPermanents.contains(row.get("name")) ){
	        		substitutes.get(season.getKey()).add(row);
	        	}
			}
		}
        return substitutes;
		
	}
	public List<Map<String,String>> getRankingSubstitutes(String season){
		return getRankingSubstitutes().get(season);
	}
	
	private void setScorers() throws java.text.ParseException {
		
		fullScorers = new HashMap<String,List<Map<String,String>>>();
		scorersByName = new HashMap<String,Map<String,Integer>>();
		scorersByDate = new HashMap<String,Map<String,Map<String,Integer>>>();
		
		for(String seasonScorerName: rawScorers.keySet()){
			String seasonName = seasonScorerName.substring("Goleadores".length(), seasonScorerName.length());
			fullScorers.put(seasonName, new ArrayList<Map<String, String>>());
			scorersByName.put(seasonName, new HashMap<String,Integer>());
			scorersByDate.put(seasonName, new HashMap<String,Map<String,Integer>>());
			List<String> scorers = new ArrayList<String>(rawScorers.get(seasonScorerName).get(0));
			scorers.remove("Fecha");
			scorers.remove("TOTAL");
			scorers.remove("CHECK");
			logger.fine(seasonScorerName+", scorers: "+scorers.size());
			for(int i = 0; i<scorers.size(); i++){//Ignore 3 first columns
				String scorer = scorers.get(i);
				Iterator<List<String>> it = rawScorers.get(seasonScorerName).iterator();
				if(it.hasNext()){it.next();}if(it.hasNext()){it.next();}//ignore first 2 rows
				int sumScore = 0;
				//Score by Dates
				while(it.hasNext()){
					List<String> l = it.next();
					if(l.isEmpty() || l.get(0)==null || "".equals(l.get(0))){ break; }
					if(l.size()<=i+3){ continue; }
					String cell = l.get(i+3);
					String date = l.get(0);
					if(i==0){
						scorersByDate.get(seasonName).put(date, new HashMap<String,Integer>());
					} 
					if(cell!=null && !"".equals(cell)){
						float cellFloat = Float.parseFloat(cell);
						sumScore += cellFloat;
						scorersByDate.get(seasonName).get(date).put(scorer, (int)cellFloat);
					}
				}
				//Score by Players
				if(sumScore>0 && !scorer.contains("Propia")){
					Map<String,String> mName = new HashMap<String,String>();
					mName.put("name", scorer);
					mName.put("scores", ""+sumScore);
					fullScorers.get(seasonName).add(mName);
					scorersByName.get(seasonName).put(scorer, sumScore);
				}
			}
			logger.fine(seasonScorerName+": "+fullScorers.get(seasonName));
		}
	}
		   

	private void setRanking() throws java.text.ParseException {
        
		Map<String,Map<String,Integer>> points = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> realPoints = new HashMap<String,Map<String,Integer>>();
//		Map<String,Map<String,Integer>> goalsFor = new HashMap<String,Map<String,Integer>>();
//		Map<String,Map<String,Integer>> goalsAgainst = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> wins = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> draws = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> loses = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> matches = new HashMap<String,Map<String,Integer>>();

		fullRanking = new HashMap<String,List<Map<String,String>>>();

		players = new HashMap<String, Set<String>>();
		
		for(String seasonName: rawMatches.keySet()){
//			Map<String, String> avgSeasonPlayerScore = avgSeasonPlayerScore(seasonName);
			
			Map<String, Integer> mvpsByPlayers = getMVPsByPlayers(seasonName);
			
			fullRanking.put(seasonName, new ArrayList<Map<String,String>>());
	        players.put(seasonName, getListOfPlayers(points, realPoints, wins, draws, loses, matches, seasonName));
	        logger.fine(seasonName+": "+players.toString());
	        for(String name : players.get(seasonName)){
	        	Map<String, String> e = new HashMap<String, String>();
	        	Integer goals = scorersByName.containsKey(seasonName)&&scorersByName.get(seasonName).containsKey(name)?scorersByName.get(seasonName).get(name):0;
	        	e.put("name", name);
	        	e.put("realPoints", realPoints.get(seasonName).get(name).toString());
	        	e.put("points", points.get(seasonName).get(name).toString());
	        	e.put("goalsFor", goals.toString());
//	        	e.put("goalsFor", goalsFor.get(seasonName).get(name).toString());
//	        	e.put("goalsAgainst", goalsAgainst.get(seasonName).get(name).toString());
	        	e.put("wins", wins.get(seasonName).containsKey(name)?wins.get(seasonName).get(name).toString():"0");
	        	e.put("draws", draws.get(seasonName).containsKey(name)?draws.get(seasonName).get(name).toString():"0");
	        	e.put("loses", loses.get(seasonName).containsKey(name)?loses.get(seasonName).get(name).toString():"0");
	        	e.put("matches", matches.get(seasonName).get(name).toString());
	        	String lastMatches = getLastMatches(name,seasonName);
	        	e.put("lastMatches", lastMatches);
	        	e.put("trendingMatches", lastMatches);
	        	e.put("MVPs", ""+(mvpsByPlayers.containsKey(name)?mvpsByPlayers.get(name):0));
	        	boolean valid = matches.get(seasonName).get(name)>=(numMatches.get(seasonName)*MIN_VALID_MATCHES);
	        	e.put("pointsAVG", (valid?new Float(realPoints.get(seasonName).get(name)*1.0F/matches.get(seasonName).get(name)):"").toString());
	        	e.put("goalsForAVG", (valid?new Float(goals*1.0F/matches.get(seasonName).get(name)):"").toString());
//	        	e.put("goalsForAVG", (valid?new Float(goalsFor.get(seasonName).get(name)*1.0F/matches.get(seasonName).get(name)):"").toString());
//	        	e.put("goalsAgainstAVG", (valid?new Float(goalsAgainst.get(seasonName).get(name)*1.0F/matches.get(seasonName).get(name)):new Float(99.99F)).toString());
//	        	e.put("scoreAVG", (valid&&avgSeasonPlayerScore.containsKey(name))?avgSeasonPlayerScore.get(name):"0");
	        	fullRanking.get(seasonName).add(e);
	        }
		}
	}
	
	/**
	 * @deprecated
	 */
	private Map<String, String> avgSeasonPlayerScore(String seasonName){
		Document d = DBConnector.getVotes(seasonName);
		Map<String, String> avgSeasonPlayerScore = new HashMap<String,String>();
		Map<String, List<Double>> listSeasonPlayerScore = new HashMap<String,List<Double>>();
		for(Entry<String, Object> date : d.entrySet()){
			List<Document> scores = (List<Document>) ((Document)date.getValue()).get("scores");
			List<Document> scoresAVG = (List<Document>) ((Document)date.getValue()).get("scoresAVG");
			if(scores!=null){
				Map<String, List<Double>> temp = new HashMap<String,List<Double>>();
				for(Document score : scores){
					String voted = score.getString("voted");
					Double scoreD = score.get("score") instanceof Integer? score.getInteger("score") : score.getDouble("score");
					if(!temp.containsKey(voted)){
						List<Double> l = new ArrayList<Double>();
						temp.put(voted, l);
					}
					temp.get(voted).add(scoreD);
				}
				for(Entry<String, List<Double>> dateScores : temp.entrySet()){
					String voted = dateScores.getKey();
					if(!listSeasonPlayerScore.containsKey(voted)){
						List<Double> l = new ArrayList<Double>();
						listSeasonPlayerScore.put(voted, l);
					}
					Stream<Double> stream = dateScores.getValue().stream();
					if(dateScores.getValue().size()>3){
						stream = stream.sorted()
						.skip(1)//Ignore higher punctuation far AVG
						.sorted(Comparator.reverseOrder())
						.skip(1);//Ignore lower punctuation for AVG
					}
					double avg = stream.mapToDouble(new ToDoubleFunction<Double>() {
								@Override
								public double applyAsDouble(Double value) {
									return value;
								}
							})
							.average()
							.getAsDouble();
					listSeasonPlayerScore.get(voted).add(avg);
				}
			}else if(scoresAVG!=null){
				for(Document score : scoresAVG){
					String voted = score.getString("voted");
					Double scoreFubles = score.getDouble("scoreFubles");
					if(!listSeasonPlayerScore.containsKey(voted)){
						List<Double> l = new ArrayList<Double>();
						listSeasonPlayerScore.put(voted, l);
					}
					listSeasonPlayerScore.get(voted).add(3.0*scoreFubles/2.0-5);
				}
			}
		}
		for(Entry<String, List<Double>> seasonPlayerScore : listSeasonPlayerScore.entrySet()){
			avgSeasonPlayerScore.put(seasonPlayerScore.getKey(),""+
					seasonPlayerScore.getValue().stream().mapToDouble(new ToDoubleFunction<Double>() {
						@Override
						public double applyAsDouble(Double value) {
							return value;
						}
					}).average().getAsDouble());
		}
		return avgSeasonPlayerScore;
	}

	private String getLastMatches(String name, String seasonName) {
		String ret = "";
		for(List<String> row: rawMatches.get(seasonName)){
			if(row.contains("Fecha")) continue;
			if(isRowEmpty(row)) break;
			if(row.contains(name)){
				int i = row.indexOf(name);
				int gFor = ((i<8)?Math.round(Float.parseFloat(row.get(8))):Math.round(Float.parseFloat(row.get(9))));
				int gAgainst = ((i<8)?Math.round(Float.parseFloat(row.get(9))):Math.round(Float.parseFloat(row.get(8))));
				if(gFor>gAgainst) ret += "w";
				if(gFor<gAgainst) ret += "l";
				if(gFor==gAgainst) ret += "d";
			}else{
				ret += "-";
			}
		}
		return ret;
	}

	public static List<List<String>> formatCSVdata(InputStream is) throws IOException {
		List<List<String>> table = new ArrayList<List<String>>();
		String line = "";
		String cvsSplitBy = ",";
		BufferedReader  br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		while ((line = br.readLine()) != null) {
			List<String> row = Arrays.asList(line.split(cvsSplitBy));
			table.add(row);
		}
		return table;
	}

	public static Map<String,List<List<String>>> formatPOIdata(InputStream is) throws IOException {
		Workbook wb = new XSSFWorkbook(is);
		Map<String,List<List<String>>> allTables = new HashMap<String,List<List<String>>>();
		Iterator<Sheet> itSheets = wb.sheetIterator();
		while(itSheets.hasNext()){
			Sheet sh = itSheets.next();
			Iterator<Row> itRows = sh.rowIterator();
			List<List<String>> table = new ArrayList<List<String>>();
			while (itRows.hasNext()) {
				Row r = itRows.next();
				if(r.getLastCellNum()<4) break;
				Iterator<Cell> itCells = r.cellIterator();
				List<String> row = new ArrayList<String>();
				int col = 0;
				while (itCells.hasNext()) {
					Cell c = itCells.next();
					while(c.getColumnIndex()>col){
						row.add("");
						col++;
					}
					row.add(c.toString());
					col++;
				}
				table.add(row);
			}
			allTables.put(sh.getSheetName(), table);
		}
		wb.close();
		return allTables;
	}

	private Set<String> getListOfPlayers(Map<String,Map<String,Integer>> points,
										Map<String,Map<String,Integer>> realPoints,
//										Map<String,Map<String,Integer>> goalsFor,
//										Map<String,Map<String,Integer>> goalsAgainst,
										Map<String,Map<String,Integer>> wins,
										Map<String,Map<String,Integer>> draws,
										Map<String,Map<String,Integer>> loses,
										Map<String,Map<String,Integer>> numMatches,
										String seasonName) throws java.text.ParseException {
		Set<String> names = new TreeSet<String>();
		points.put(seasonName, new HashMap<String,Integer>());
		realPoints.put(seasonName, new HashMap<String,Integer>());
//		goalsFor.put(seasonName, new HashMap<String,Integer>());
//		goalsAgainst.put(seasonName, new HashMap<String,Integer>());
		wins.put(seasonName, new HashMap<String,Integer>());
		draws.put(seasonName, new HashMap<String,Integer>());
		loses.put(seasonName, new HashMap<String,Integer>());
		numMatches.put(seasonName, new HashMap<String,Integer>());
		for(List<String> row: rawMatches.get(seasonName)){
			if(row.contains("Fecha")) continue;
			if(isRowEmpty(row)) break;
//			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//			Date date = formatter.parse(row.get(0));
			Integer goalsA = Math.round(Float.parseFloat(row.get(8)));
			Integer goalsB = Math.round(Float.parseFloat(row.get(9)));
			for(int i=0;i<row.size();i++){
				String player = row.get(i);
				if(i!=0&&i!=8&&i!=9&&i!=17){
					names.add(row.get(i));
					int gFor = ((i<8)?goalsA:goalsB);
					int gAgainst = ((i<8)?goalsB:goalsA);
					int pointsM = ((gFor>gAgainst)?3:((gFor<gAgainst)?1:2));
					int pointsN = ((gFor>gAgainst)?3:((gFor<gAgainst)?0:1));
//					goalsFor.get(seasonName).put(player, (goalsFor.get(seasonName).get(player)!=null?
//														goalsFor.get(seasonName).get(player):0) + gFor );
//					goalsAgainst.get(seasonName).put(player, (goalsAgainst.get(seasonName).get(player)!=null?
//															goalsAgainst.get(seasonName).get(player):0) + gAgainst );
					points.get(seasonName).put(player, (points.get(seasonName).get(player)!=null?
														points.get(seasonName).get(player):0) + pointsM );
					realPoints.get(seasonName).put(player, (realPoints.get(seasonName).get(player)!=null?
															realPoints.get(seasonName).get(player):0) + pointsN );
					switch (pointsM) {
			            case 3:  wins.get(seasonName).put(player, (wins.get(seasonName).get(player)!=null?
			            											wins.get(seasonName).get(player):0) + 1 );
			                     break;
			            case 2:  draws.get(seasonName).put(player, (draws.get(seasonName).get(player)!=null?
			            											draws.get(seasonName).get(player):0) + 1 );
			                     break;
			            case 1:  loses.get(seasonName).put(player, (loses.get(seasonName).get(player)!=null?
			            											loses.get(seasonName).get(player):0) + 1 );
			                     break;
					}
					numMatches.get(seasonName).put(player, (numMatches.get(seasonName).get(player)!=null?
															numMatches.get(seasonName).get(player):0) + 1 );
				}
			}
		}
		return names;
	}

	public Map<String, Set<String>> getPlayers() {		
		return players;
	}
	public Set<String> getPlayers(String season) {		
		return players.get(season);
	}

	public Map<String,Integer> getComparison(JsonNode jsonNode) {
		
		Map<String,Integer> res = new HashMap<String,Integer>();
		res.put("sameWin", 0);
		res.put("sameDraw", 0);
		res.put("sameLose", 0);
		res.put("againstWin", 0);
		res.put("againstDraw", 0);
		res.put("againstLose", 0);
		
		String season = jsonNode.findValue("season").asText();
		String playerOne = jsonNode.findValue("playerOne").asText();
		String playerTwo = jsonNode.findValue("playerTwo").asText();
		
		for(List<String> row: rawMatches.get(season)){
			
			boolean p1w=false, p1b=false, p2w=false, p2b=false;
			if(isRowEmpty(row)) break;
			for(int i=1;i<8;i++){
				if(playerOne.equals(row.get(i))){
					p1b=true;
				}else if(playerTwo.equals(row.get(i))){
					p2b=true;
				}
			}
			int dif = Math.round( Float.parseFloat(row.get(8))-Float.parseFloat(row.get(9)) );//>1 Blue win
			for(int j=10;j<17;j++){
				if(playerOne.equals(row.get(j))){
					p1w=true;
				}else if(playerTwo.equals(row.get(j))){
					p2w=true;
				}
			}
			
			if(p1b==true && p2b==true){
				if(dif>0){
					res.put("sameWin", res.get("sameWin")+1);
				}else if(dif<0){
					res.put("sameLose", res.get("sameLose")+1);
				}else{
					res.put("sameDraw", res.get("sameDraw")+1);
				}
			}
			
			if(p1w==true && p2w==true){
				if(dif>0){
					res.put("sameLose", res.get("sameLose")+1);
				}else if(dif<0){
					res.put("sameWin", res.get("sameWin")+1);
				}else{
					res.put("sameDraw", res.get("sameDraw")+1);
				}
			}
			
			if(p1w==true && p2b==true){
				if(dif>0){
					res.put("againstLose", res.get("againstLose")+1);
				}else if(dif<0){
					res.put("againstWin", res.get("againstWin")+1);
				}else{
					res.put("againstDraw", res.get("againstDraw")+1);
				}
			}
			
			if(p1b==true && p2w==true){
				if(dif>0){
					res.put("againstWin", res.get("againstWin")+1);
				}else if(dif<0){
					res.put("againstLose", res.get("againstLose")+1);
				}else{
					res.put("againstDraw", res.get("againstDraw")+1);
				}
			}
		}
		
		return res;
	}

	private static boolean isRowEmpty(List<String> row){
		return (row==null || row.isEmpty() || row.size()<13 || "".equals(row.get(0)));
	}

	@SuppressWarnings("unchecked")
	public Object getLastMatchResult(JsonNode jsonNode) {
		List<Map.Entry<String,Map<String,Object>>> result = new ArrayList<Map.Entry<String,Map<String,Object>>>();

		Map<String,Map<String,Object>> result2 = new TreeMap<String,Map<String,Object>>();

		try{
			Long dateL = jsonNode.get("match").get("day").asLong();
			String date = formatter2.format(new Date(dateL));
			String date2 = formatter.format(new Date(dateL));
			String season = jsonNode.get("season").asText();
			List<String> teamBlue = new ArrayList<String>(), teamWhite = new ArrayList<String>();
			if(jsonNode.get("match").get("data")!=null){
				for(JsonNode teamPair : jsonNode.get("match").get("data")){
					teamWhite.add(teamPair.get("white").asText());
					teamBlue.add(teamPair.get("blue").asText());
				}
			}
			Document votes = DBConnector.getVotes(season, date);
			if(votes==null){ 
//				logger.warning("getVotes didn't return any result for season "+season+", date "+date); 
				return null; }
			Map<String,String> playersPictures = DBConnector.getPlayersPictures();
			List<Document> scores = (List<Document>) votes.get("scores");
			List<Document> scoresAVG = (List<Document>) votes.get("scoresAVG");
			if(scores!=null && !scores.isEmpty()){
				for(Document score : scores){
					String voter = score.getString("voter");
					String voted = score.getString("voted");
					Double scoreD = score.get("score") instanceof Integer? score.getInteger("score") : score.getDouble("score");
					String comment = score.getString("comment");
					Map<String,Object> punctuation = new HashMap<String,Object>();
					punctuation.put("voter", voter);
					punctuation.put("score", scoreD);
					punctuation.put("comment", comment);
					if(result2.containsKey(voted)){
						((List<Double>)result2.get(voted).get("avgList")).add(scoreD);
						((List<Map<String,Object>>)result2.get(voted).get("punctuations")).add(punctuation);
					}else{
						List<Map<String,Object>> punctuations = new  ArrayList<Map<String,Object>>();
						punctuations.add(punctuation);
						List<Double> avgList = new ArrayList<Double>();
						avgList.add(scoreD);
						Map<String,Object> punctuationData = new HashMap<String,Object>();
						punctuationData.put("punctuations", punctuations);
						punctuationData.put("avgList", avgList);
						punctuationData.put("team", teamBlue.contains(voted)?"blue":teamWhite.contains(voted)?"white":"");
//						punctuationData.put("avg", (double)scoreI);
						if(playersPictures.containsKey(voted)){ punctuationData.put("image", playersPictures.get(voted)); }
						if(scorersByDate!=null && scorersByDate.get(season)!=null && 
							scorersByDate.get(season).get(date2)!=null){
							punctuationData.put("scores",scorersByDate.get(season).get(date2).get(voted));
						}
						result2.put(voted,punctuationData);
					}
				}
				for(String voted : result2.keySet()){
					Stream<Double> stream = ((List<Double>)result2.get(voted).get("avgList")).stream();
					if(((List<Double>)result2.get(voted).get("avgList")).size()>3){
						stream = stream.sorted()
						.skip(1)//Ignore higher punctuation far AVG
						.sorted(Comparator.reverseOrder())
						.skip(1);//Ignore lower punctuation for AVG
					}
					double avg = stream.mapToDouble(new ToDoubleFunction<Double>() {
								@Override
								public double applyAsDouble(Double value) {
									return value;
								}
							})
							.average()
							.getAsDouble();
					result2.get(voted).put("avg",avg);
				}
			}else if(scoresAVG!=null && !scoresAVG.isEmpty()){//Only for scores from fubles
				for(Document score : scoresAVG){
					String voted = score.getString("voted");
					Double scoreD = score.getDouble("score");
					Double scoreFublesD = score.getDouble("scoreFubles");
					Map<String,Object> punctuationData = new HashMap<String,Object>();
					punctuationData.put("linkFubles", score.getString("linkFubles"));
					if(scoreFublesD!=null && scoreD!=null){
						punctuationData.put("avg", scoreD);
						punctuationData.put("avgFubles", scoreFublesD);
					}else if(scoreFublesD!=null){
						punctuationData.put("avg", (3.0*scoreFublesD/2.0-5));
						punctuationData.put("avgFubles", scoreFublesD);
					}else{
						punctuationData.put("avg", scoreD);
						punctuationData.put("avgFubles", (2.0*scoreD/3.0+10.0/3.0));
					}
					if(playersPictures.containsKey(voted)){ punctuationData.put("image", playersPictures.get(voted)); }
					result2.put(voted,punctuationData);
				}
			}
			//Get most titled players
			for(String titleName : TITLES){
				if(votes.containsKey(titleName)){
					List<String> titlesList = (List<String>) votes.get(titleName);
					Map<String, Long> occurrences = titlesList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					occurrences.remove("");
					if(!occurrences.isEmpty()){
						Entry<String, Long> max = occurrences.entrySet().stream().max(Comparator.comparing(Entry::getValue)).get();
						if(max.getValue()>1){//Titles only for more than 1 vote.
							for(Entry<String, Long> e : occurrences.entrySet()){
								if(e.getValue()==max.getValue() && result2.containsKey(e.getKey())){
									result2.get(e.getKey()).put(titleName, true);
								}
							}
						}
					}
				}
			}
			
			result.addAll(result2.entrySet());
			Collections.sort(result,new Comparator<Map.Entry<String,Map<String,Object>>>(){
				@Override
				public int compare(Map.Entry<String, Map<String,Object>> o1, Map.Entry<String, Map<String,Object>> o2) {
					return (int) ( ((Double)(o2.getValue()).get("avg") - (Double)(o1.getValue()).get("avg"))*1000 );
				}
				
			});
		}catch (Exception e) {
			e.printStackTrace();
			return Document.parse("{error: 'getLastMatchResult: "+e.getMessage()+"'}");
		}
		return result;
	}

	public Object getMatchScorers(JsonNode jsonNode) {
		Long dateL = jsonNode.get("match").get("day").asLong();
		String date2 = formatter.format(new Date(dateL));
		String season = jsonNode.get("season").asText();
		return scorersByDate!=null && scorersByDate.get(season)!=null?scorersByDate.get(season).get(date2):null;
	}

	public synchronized Object savePolling(JsonNode jsonNode) {

		try{
			Long dateL = jsonNode.get("date").asLong();
			String date = formatter2.format(new Date(dateL));
			String season = jsonNode.get("season").asText();
			String voter = jsonNode.get("voter").asText();
			String voted = jsonNode.get("voted").asText();
			if(voter==null || voted==null){
				logger.warning("No voter neither voted player "+date+" "+season+" "+voter+" "+voted);
				return null;
			}
			Document votes = DBConnector.getVotes(season, date);
			if(votes==null){
				Document d = new Document();
				d.put("season", season);
				d.put("date", date);
				DBConnector.createScore(d);
			}
			Document hasVoted = DBConnector.hasVoted(jsonNode);
			if(hasVoted==null){
				if(!DBConnector.addPunctuation(voter, voted, dateL, season)){
					logger.warning("Error trying to insert into DB: "+voter+" votes "+voted+" for match "+date+" "+season);
				}
			}else{
				logger.warning(voter+" already voted "+voted+" for match "+date+" "+season);
			}
			logger.info(voter+" has voted "+voted+", for match "+date+" "+season);
		}catch (Exception e) {
			e.printStackTrace();
			return Document.parse("{error: 'savePolling: "+e.getMessage()+"'}");
		}
		return null;
	}
	
	public Object login(JsonNode jsonNode){
		return DBConnector.getPlayer(jsonNode);
	}
	
	public Object hasVoted(JsonNode jsonNode){
		return DBConnector.hasVoted(jsonNode);
	}
	
	public Object getPlayersPictures() {		
		return DBConnector.getPlayersPictures();
	}

	public List<Map<String, String>> getUserMatches(String season, String user) {
		List<Map<String, String>> res = new ArrayList<Map<String,String>>();
		Map<String, Set<String>> mvps = getMVPs(season);
		
		try{
			for(List<String> match : rawMatches.get(season)){
				if(isRowEmpty(match)) break;
				
				Map<String, String> formattedMatch = new HashMap<String, String>();
				String date = match.get(0);
				String formattedDater = formatter2.format(formatter.parse(date));
				formattedMatch.put("date", formattedDater);
				formattedMatch.put("team", "");
				for(int i=1;i<8;i++){
					if(user.equals(match.get(i))){
						formattedMatch.put("team", "blue");
						break;
					}
					if(user.equals(match.get(i+9))){
						formattedMatch.put("team", "white");
						break;
					}
				}
				
				formattedMatch.put("result",  match.get(8)+","+match.get(9) );
				formattedMatch.put("goals", (scorersByDate.get(season)!=null && 
											scorersByDate.get(season).get(date)!=null && 
											scorersByDate.get(season).get(date).get(user)!=null)?
											scorersByDate.get(season).get(date).get(user)+"": "");
				formattedMatch.put("mvps", mvps.containsKey(formattedDater)?""+mvps.get(formattedDater).contains(user):"");
//				ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
//				jsonNode.put("season", season);
//				ObjectNode day = new ObjectNode(JsonNodeFactory.instance);
//				day.put("day", formatter.parse(date).getTime());
//				jsonNode.put("match", day);
//				List<Map.Entry<String,Map<String,Object>>> o = (List<Entry<String, Map<String, Object>>>) getLastMatchResult(jsonNode);
//				if(o!=null){
//					for(Map.Entry<String,Map<String,Object>> e : o){
//						if(user.equals(e.getKey())){
//							formattedMatch.put("score", String.format("%,.2f",e.getValue().get("avg")));
//							formattedMatch.put("titles",  "" );
//							break;
//						}
//					}
//				}
				res.add(0,formattedMatch);
			}
		
		}catch(Exception e){
			Log.warn("Error getting user matches: "+e.getMessage());
			e.printStackTrace();
		}
	
		return res;
	}
	
	public synchronized Map<String, Set<String>> getMVPs(String seasonName) {
		Document d = DBConnector.getVotes(seasonName);
		Map<String, Set<String>> listSeasonPlayerScore = new HashMap<String,Set<String>>();
		try {
			for(Entry<String, Object> dateVotes : d.entrySet()){
				if(formatter2.parse(dateVotes.getKey()).getTime()+pollingLimit<new Date().getTime()) {
					List<Document> scores = (List<Document>) ((Document)dateVotes.getValue()).get("scoresMVP");
					if(scores!=null && scores.size()>=5){
						Map<String, Integer> temp = new HashMap<String,Integer>();
						for(Document score : scores){
							String voted = score.getString("voted");
							temp.put(voted, (temp.containsKey(voted)? temp.get(voted): 0) + 1);
						}
						Set<String> mvps = new HashSet<String>();  
						int max = 2;
						for(Entry<String, Integer> scoresMVP : temp.entrySet()){
							if(scoresMVP.getValue()>max) {
								max = scoresMVP.getValue();
								mvps = new HashSet<String>();
								mvps.add(scoresMVP.getKey());
							}else if(scoresMVP.getValue()==max) {
								mvps.add(scoresMVP.getKey());
							}
						}
						listSeasonPlayerScore.put(dateVotes.getKey(), mvps);
					}
				}
			}
		}catch(Exception e) {
			Log.warn("Error getting MVP: "+e.getMessage());
			e.printStackTrace();
		}
		return listSeasonPlayerScore;
	}
	
	public synchronized Map<String, Integer> getMVPsByPlayers(String seasonName) {
		Map<String, Integer> mvpsByPlayers =  new HashMap<String, Integer>();
		Map<String, Set<String>> listSeasonPlayerScore = getMVPs(seasonName);
		
		for(Entry<String, Set<String>> dateMVPs : listSeasonPlayerScore.entrySet()){
			for(String playerMVP : dateMVPs.getValue()) {
				mvpsByPlayers.put(playerMVP, (mvpsByPlayers.containsKey(playerMVP)? mvpsByPlayers.get(playerMVP): 0) + 1);
			}
		}
		
		return mvpsByPlayers;
	}
}

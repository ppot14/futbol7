package com.ppot14.futbol7;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.function.ToDoubleFunction;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
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

public class APIUtil {
	
	private static final Logger logger = Logger.getLogger(APIUtil.class.getName());
	
	private static Map<String,List<String>> PERMANENTS = null;

	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
	private static SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy");
	
	private static Map<String,Object> config = null;
	
	private Map<String,List<Map<String,String>>> fullRanking = null;
	private Map<String,List<Map<String,String>>> fullScorers = null;
	private static Map<String,Map<String,Map<String,Integer>>> fullScorersByDate = null;
	private Map<String,Integer> numMatches = new HashMap<String, Integer>();
	private Map<String,Integer> numScorers = new HashMap<String, Integer>();
	private Map<String,List<List<String>>> rawMatches = null;
	private Map<String,List<List<String>>> rawScorers = null;
	private Map<String,Set<String>> players = null;	
	
	public APIUtil(){
		this("config.json");
	}
	
	public APIUtil(String propFileName){
		
		config = null;
        ObjectMapper mapper = new ObjectMapper();
		InputStream inputStream = APIUtil.class.getClassLoader().getResourceAsStream(propFileName);

		try {
			if (inputStream != null) {
				config = mapper.readValue(inputStream, Map.class);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PERMANENTS = (Map<String,List<String>>) config.get("permanents");
//		logger.info("PERMANENTS: "+PERMANENTS);
		
	}
	
	public static Map<String, Object> getConfig() {
		return config;
	}

	public Object getOptions() {
		Map<String, Object> options = new HashMap<String,Object>();
		options.put("permanents", PERMANENTS);
		return options;
	}

	public synchronized boolean processData(boolean refresh){
    	
		try{
			
			if(rawMatches==null || refresh){
			
				InputStream inputStream = null;
				try{
					inputStream = GoogleImporter.importFromGoogleDrive(config,null);
				}catch(SocketTimeoutException e){
					logger.severe("Error downloading from Google Drive: "+e.getMessage());
					return false;
				}
//				finally{
//					if(inputStream==null){
//						logger.info("Importing CSV from local: "+"Futbol7.csv");
//						ClassLoader classLoader = APIUtil.class.getClassLoader();
//						inputStream = classLoader.getResourceAsStream("Futbol7.csv");
//					}
//				}
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
//				logger.info("Num of Matches: "+numMatches);
//				logger.info("Matches: "+rawMatches);
//				logger.info("Num of Scorers: "+numScorers);
//				logger.info("Scorers: "+rawScorers);
				
				fullRanking = getRanking();
				fullScorers = getScorers();
				fullScorersByDate = getScorersByDate();
				
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
	public Map<String,List<Map<String, String>>> getFullRanking() {
		return fullRanking;
	}
	
	/**
	 * @return the fullRanking
	 */
	public Map<String, List<Map<String, String>>> getFullScorers() {
		return fullScorers;
	}

	public void writeToFileAndServer(){

		File project =  new File("");
		
		String jsonSrcFolder = project.getAbsolutePath()+"\\src\\main\\webapp\\resources\\json\\";
		
//		logger.info("Project src json folder: "+jsonSrcFolder);

		try{
			
			writeToServer(writeJSONtoFile(jsonSrcFolder,"full.js",jsonToString(getFullRanking())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"pair.js",jsonToString(getPair())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"permanents.js",jsonToString(getRankingPermanents())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"substitutes.js",jsonToString(getRankingSubstitutes())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"vs.js",jsonToString(getVS())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"pointsSeries.js",jsonToString(getPointsSeries())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"matches.js",jsonToString(getResults())),"/resources/json");
	        
		}catch(Exception e){
			e.printStackTrace();
		}
		writeToServer(new File(project.getAbsolutePath()+"\\src\\main\\webapp\\index.html"),"");
		
	}

	public Map<String,List<Map<String, Object>>> getResults() throws ParseException {
		Map<String,List<Map<String, Object>>> res = new HashMap<String,List<Map<String,Object>>>();
		
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
		
		return res;
	}

	public Map<String,List<Map<String, Object>>> getPointsSeries() throws ParseException {
		Date today = new Date();
		Map<String,List<Map<String, Object>>> data = new HashMap<String,List<Map<String,Object>>>();

		for(Entry<String, List<List<String>>> seasonMatches: rawMatches.entrySet()){
			String season = seasonMatches.getKey();
			data.put(season, new ArrayList<Map<String, Object>>());
			for(String name : players.get(season)){
    			int points = 0;
            	List<List<Object>> playerData = new ArrayList<List<Object>>();
    			for(List<String> row : seasonMatches.getValue()){
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
							int pointsM = ((gFor>gAgainst)?3:((gFor<gAgainst)?1:2));
		        			points += pointsM;
		        			game.add(points);
		        			playerData.add(game);
		        		}else{
		        			continue;
		        		}
	        		}
	        		
	        	}

            	//Hack to add current points at current day to the current season
    			if( season.contains(Integer.toString(Calendar.getInstance().get(Calendar.YEAR))) ){
    	        	Integer maxPts = (Integer) playerData.get(playerData.size()-1).get(1);
    				List<Object> game = new ArrayList<Object>();
    				game.add(today);
    				game.add(maxPts);
    				playerData.add(game);
    			}
    			
            	Map<String, Object> e = new HashMap<String, Object>();
            	e.put("name", name);
            	e.put("data", playerData);
        		data.get(season).add(e);
    			
    		}
        	
        }
        
		return data;
	}

	private File writeJSONtoFile(String folder, String file, String jsonString) throws IOException {
		String varName = file.split("\\.")[0];
//		logger.info("File created: "+folder+file+"; var "+varName+" = "+ jsonString+ ";");
		List<String> lines = Arrays.asList("var "+varName+" = ", jsonString, ";");
		Path path = Paths.get(folder+file);
		Files.write(path, lines, Charset.forName("UTF-8"));
		return path.toFile();
	}
	
	private void writeToServer(File file, String subdir){
			String SFTPHOST = (String) config.get("sftp-host");
			int    SFTPPORT = (Integer) config.get("sftp-port");
			String SFTPUSER = (String) config.get("sftp-username");
			String SFTPPASS = (String) config.get("sftp-password");
			String SFTPWORKINGDIR = (String) config.get("sftp-directory") + subdir;
			 
		    FTPClient ftp = new FTPClient();
		    ftp.setConnectTimeout(10000);
			 
			try{
			      int reply;
			      ftp.connect(SFTPHOST,SFTPPORT);
			      ftp.login(SFTPUSER, SFTPPASS);
			      reply = ftp.getReplyCode();

			      if(!FTPReply.isPositiveCompletion(reply)) {
			        ftp.disconnect();
					logger.severe("FTP server refused connection: "+ftp.getReplyString());
			        return;
			      }
			      
			      ftp.changeWorkingDirectory(SFTPWORKINGDIR);
			      ftp.enterLocalPassiveMode();
			      
			      FileInputStream fis = new FileInputStream(file);
			      ftp.storeFile(file.getName(), fis);

			      reply = ftp.getReplyCode();
			      if(!FTPReply.isPositiveCompletion(reply)) {
			        ftp.disconnect();
					logger.severe("FTP file transfer error: "+reply+" "+ftp.getReplyString());
			        return;
			      }
			      
			      fis.close();
		          ftp.logout();				
				logger.info("File "+file.getName()+" transfered to server folder "+SFTPWORKINGDIR);
			}catch(Exception ex){
				logger.severe("Impossible to transfer file to server: "+ex);
				ex.printStackTrace();
			}finally {
		      if(ftp.isConnected()) {
		          try {
		            ftp.disconnect();
		          } catch(IOException ioe) {
		            // do nothing
		          }
		       }
		    }
	}

	public Map<String,List<Map<String,String>>> getVS() throws java.text.ParseException {
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
	        	Map<String, String> e = new HashMap<String, String>();
	        	e.put("player1", (String)row.getKey().toArray()[0]);
	        	e.put("player2", (String)row.getKey().toArray()[1]);
	        	e.put("vs", row.getValue().toString());
				data.get(seasonMatches.getKey()).add(e);
	        }
		}
		
		return data;
		
	}

	public Map<String,List<Map<String,String>>> getPair() throws java.text.ParseException {
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
	        	Map<String, String> e = new HashMap<String, String>();
	        	e.put("player1", (String)row.getKey().toArray()[0]);
	        	e.put("player2", (String)row.getKey().toArray()[1]);
	        	e.put("pair", row.getValue().toString());
				data.get(seasonMatches.getKey()).add(e);
	        }
		}
		return data;
		
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
	
	private Map<String,List<Map<String, String>>> getScorers() throws java.text.ParseException {
		
		Map<String, List<Map<String, String>>> data = new HashMap<String,List<Map<String,String>>>();
		
		for(String seasonScorerName: rawScorers.keySet()){
			String seasonName = seasonScorerName.substring("Goleadores".length(), seasonScorerName.length());
			data.put(seasonName, new ArrayList<Map<String, String>>());
			List<String> scorers = new ArrayList<String>(rawScorers.get(seasonScorerName).get(0));
			scorers.remove("Fecha");
			scorers.remove("TOTAL");
			scorers.remove("CHECK");
//			rawScorers.get(seasonScorerName).remove(0);
//			rawScorers.get(seasonScorerName).remove(0);
//			Iterator<List<String>> it = rawScorers.get(seasonScorerName).iterator();
			for(int i = 3; i<scorers.size(); i++){//Ignore 3 first columns
				Iterator<List<String>> it = rawScorers.get(seasonScorerName).iterator();
				if(it.hasNext()){it.next();}if(it.hasNext()){it.next();}//ignore first 2 rows
				int sumScore = 0;
				while(it.hasNext()){
					List<String> l = it.next();
					if(l.size()<=i){ continue; }
					String cell = l.get(i);
					if(cell!=null && !"".equals(cell)){
						sumScore += Float.parseFloat(cell);
					}
				}
				if(sumScore>0 && !scorers.get(i-3).contains("Propia")){
					Map<String,String> mName = new HashMap<String,String>();
					mName.put("name", scorers.get(i-3));
					mName.put("scores", ""+sumScore);
					data.get(seasonName).add(mName);
				}
			}
			logger.fine(seasonScorerName+": "+data.get(seasonName));
		}
		
		return data;
	}
	
	private Map<String,Map<String, Map<String, Integer>>> getScorersByDate() throws java.text.ParseException {
		
		Map<String,Map<String, Map<String, Integer>>> data = new HashMap<String,Map<String, Map<String, Integer>>>();
		
		for(String seasonScorerName: rawScorers.keySet()){
			String seasonName = seasonScorerName.substring("Goleadores".length(), seasonScorerName.length());
			data.put(seasonName, new HashMap<String,Map<String, Integer>>());
			List<String> scorers = new ArrayList<String>(rawScorers.get(seasonScorerName).get(0));
//			scorers.remove("Fecha");
//			scorers.remove("TOTAL");
//			scorers.remove("CHECK");
//			rawScorers.get(seasonScorerName).remove(0);
//			rawScorers.get(seasonScorerName).remove(0);
			for(int i = 2; i<rawScorers.get(seasonScorerName).size(); i++){
				List<String> row = rawScorers.get(seasonScorerName).get(i);
				String date = rawScorers.get(seasonScorerName).get(i).get(0);
				data.get(seasonName).put(date, new HashMap<String,Integer>());
				for(int j = 3; j<rawScorers.get(seasonScorerName).get(i).size(); j++){
					int s = (int)Float.parseFloat(!"".equals(rawScorers.get(seasonScorerName).get(i).get(j))?
													rawScorers.get(seasonScorerName).get(i).get(j): "0");
					data.get(seasonName).get(date).put(scorers.get(j), s);
				}
			}
			logger.fine(seasonScorerName+": "+data.get(seasonName));
		}
		
		return data;
	}	
		   

	private Map<String,List<Map<String, String>>> getRanking() throws java.text.ParseException {
        
		Map<String,Map<String,Integer>> points = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> realPoints = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> goalsFor = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> goalsAgainst = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> wins = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> draws = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> loses = new HashMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> matches = new HashMap<String,Map<String,Integer>>();

		Map<String,List<Map<String,String>>> data = new HashMap<String,List<Map<String,String>>>();

		players = new HashMap<String, Set<String>>();
		
		for(String seasonName: rawMatches.keySet()){
			data.put(seasonName, new ArrayList<Map<String,String>>());
	        players.put(seasonName, getListOfPlayers(points, realPoints, goalsFor, goalsAgainst, wins, draws, loses, matches, seasonName));
	        logger.fine(seasonName+": "+players.toString());
	        for(String name : players.get(seasonName)){
	        	Map<String, String> e = new HashMap<String, String>();
	        	e.put("name", name);
	        	e.put("realPoints", realPoints.get(seasonName).get(name).toString());
	        	e.put("points", points.get(seasonName).get(name).toString());
	        	e.put("goalsFor", goalsFor.get(seasonName).get(name).toString());
	        	e.put("goalsAgainst", goalsAgainst.get(seasonName).get(name).toString());
	        	e.put("wins", wins.get(seasonName).containsKey(name)?wins.get(seasonName).get(name).toString():"0");
	        	e.put("draws", draws.get(seasonName).containsKey(name)?draws.get(seasonName).get(name).toString():"0");
	        	e.put("loses", loses.get(seasonName).containsKey(name)?loses.get(seasonName).get(name).toString():"0");
	        	e.put("matches", matches.get(seasonName).get(name).toString());
	        	e.put("lastMatches", getLastMatches(name,seasonName));
	        	//*(matches.get(name)<(numMatches/3)?0:1.0F) in case of less than 1/4 of total match the avg is 0 or 99
	        	boolean valid = matches.get(seasonName).get(name)>=(numMatches.get(seasonName)*1.0/4);
	        	e.put("pointsAVG", (valid?new Float(realPoints.get(seasonName).get(name)*1.0F/matches.get(seasonName).get(name)):"").toString());
	        	e.put("goalsForAVG", (valid?new Float(goalsFor.get(seasonName).get(name)*1.0F/matches.get(seasonName).get(name)):"").toString());
	        	e.put("goalsAgainstAVG", (valid?new Float(goalsAgainst.get(seasonName).get(name)*1.0F/matches.get(seasonName).get(name)):new Float(99.99F)).toString());
				data.get(seasonName).add(e);
	        }
		}
		return data;
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
										Map<String,Map<String,Integer>> goalsFor,
										Map<String,Map<String,Integer>> goalsAgainst,
										Map<String,Map<String,Integer>> wins,
										Map<String,Map<String,Integer>> draws,
										Map<String,Map<String,Integer>> loses,
										Map<String,Map<String,Integer>> numMatches,
										String seasonName) throws java.text.ParseException {
		Set<String> names = new TreeSet<String>();
		points.put(seasonName, new HashMap<String,Integer>());
		realPoints.put(seasonName, new HashMap<String,Integer>());
		goalsFor.put(seasonName, new HashMap<String,Integer>());
		goalsAgainst.put(seasonName, new HashMap<String,Integer>());
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
					goalsFor.get(seasonName).put(player, (goalsFor.get(seasonName).get(player)!=null?
														goalsFor.get(seasonName).get(player):0) + gFor );
					goalsAgainst.get(seasonName).put(player, (goalsAgainst.get(seasonName).get(player)!=null?
															goalsAgainst.get(seasonName).get(player):0) + gAgainst );
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

	public static Object getLastMatchResult(JsonNode jsonNode) {
		List<Map.Entry<String,Map<String,Object>>> result = new ArrayList<Map.Entry<String,Map<String,Object>>>();

		Map<String,Map<String,Object>> result2 = new TreeMap<String,Map<String,Object>>();

		try{
			Long dateL = jsonNode.get("match").get("day").asLong();
			String date = formatter2.format(new Date(dateL));
			String date2 = formatter.format(new Date(dateL));
			String season = jsonNode.get("season").asText();
			Document votes = DBConnector.getVotes(season, date);
			if(votes==null){ logger.warning("getVotes didn't return any result for season "+season+", date "+date); return null; }
			Map<String,String> playersPictures = DBConnector.getPlayersPictures();
			List<Document> scores = (List<Document>) votes.get("scores");
			for(Document score : scores){
				String voter = score.getString("voter");
				String voted = score.getString("voted");
				Integer scoreI = score.getInteger("score");
				String comment = score.getString("comment");
				Map<String,Object> punctuation = new HashMap<String,Object>();
				punctuation.put("voter", voter);
				punctuation.put("score", scoreI);
				punctuation.put("comment", comment);
				if(result2.containsKey(voted)){
					((List<Integer>)result2.get(voted).get("avgList")).add(scoreI);
					double newavg = ((List<Integer>)result2.get(voted).get("avgList")).stream().mapToDouble(new ToDoubleFunction<Integer>() {
						@Override
						public double applyAsDouble(Integer value) {
							return value;
						}
					}).average().getAsDouble();
					result2.get(voted).put("avg",newavg);
					((List<Map<String,Object>>)result2.get(voted).get("punctuations")).add(punctuation);
				}else{
					List<Map<String,Object>> punctuations = new  ArrayList<Map<String,Object>>();
					punctuations.add(punctuation);
					List<Integer> avgList = new ArrayList<Integer>();
					avgList.add(scoreI);
					Map<String,Object> punctuationData = new HashMap<String,Object>();
					punctuationData.put("punctuations", punctuations);
					punctuationData.put("avgList", avgList);
					punctuationData.put("avg", (double)scoreI);
					if(playersPictures.containsKey(voted)){ punctuationData.put("image", playersPictures.get(voted)); }
					if(fullScorersByDate!=null && fullScorersByDate.get(season)!=null && 
						fullScorersByDate.get(season).get(date2)!=null){
						punctuationData.put("scores",fullScorersByDate.get(season).get(date2).get(voted));
					}
					result2.put(voted,punctuationData);
				}
			}
			result.addAll(result2.entrySet());
			Collections.sort(result,new Comparator<Map.Entry<String,Map<String,Object>>>(){
				@Override
				public int compare(Map.Entry<String, Map<String,Object>> o1, Map.Entry<String, Map<String,Object>> o2) {
					return (int) ( (Double)(o2.getValue()).get("avg") - (Double)(o1.getValue()).get("avg") );
				}
				
			});
		}catch (Exception e) {
			e.printStackTrace();
			return Document.parse("{error, getLastMatchResult: '"+e.getMessage()+"'}");
		}
		return result;
	}

	public static Object getMatchScorers(JsonNode jsonNode) {
		Long dateL = jsonNode.get("match").get("day").asLong();
		String date2 = formatter.format(new Date(dateL));
		String season = jsonNode.get("season").asText();
		return fullScorersByDate.get(season)!=null?fullScorersByDate.get(season).get(date2):null;
	}

	public static Object savePolling(JsonNode jsonNode) {

		try{
			Long dateL = jsonNode.get("date").asLong();
			String date = formatter2.format(new Date(dateL));
			String season = jsonNode.get("season").asText();
			ArrayNode scores = (ArrayNode) jsonNode.get("scores");
			if(scores==null || scores.size()!=13){
				return null;
			}
			Document votes = DBConnector.getVotes(season, date);
			if(votes==null){
				Document d = Document.parse(jsonNode.toString());
				d.put("date", date);
				DBConnector.createScore(d);
			}else{
				for(JsonNode p : scores){
					String voter = p.get("voter").asText();
					String voted = p.get("voted").asText();
					Integer score = p.get("score").asInt();
					String comment = p.get("comment").asText();
					Document hasVoted = DBConnector.hasVotedPlayer(voter, voted, dateL, season);
					if(hasVoted==null){
						if(!DBConnector.addPunctuation(voter, voted, score, comment, dateL, season)){
							logger.warning("Error trying to insert into DB: "+voter+" votes "+voted+" for match "+date+" "+season);
						}
					}else{
						logger.warning(voter+" already voted "+voted+" for match "+date+" "+season);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return Document.parse("{error, savePolling: '"+e.getMessage()+"'}");
		}
		return null;
	}
}

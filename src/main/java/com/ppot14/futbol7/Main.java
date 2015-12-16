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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Main {
	
	private static List<String> PERMANENTS = null;
	
	private static final Logger logger = Logger.getLogger(Main.class.getName());

	SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	
	private static Map<String,Object> config = null;
	
	private List<Map<String,String>> fullRanking = null;
	private List<Map<String,Object>> results = null;
	private int numMatches = 0;
	private List<List<String>> matches = null;
	private List<Map<String,Object>> pointsSeries = null;
	private Set<String> players = null;	

	public static void main(String[] args) throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		Main m = new Main();
		m.init();
		m.run();
		
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);  
		
		logger.info("Futbol7 executed in "+duration/1000+"s");
	}

	public void init() {
		
		config = null;
		String propFileName = "config.json";
        ObjectMapper mapper = new ObjectMapper();
		InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(propFileName);

		try {
			if (inputStream != null) {
				config = mapper.readValue(inputStream, Map.class);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
    	
		try{
			
			PERMANENTS = (List<String>) config.get("permanents");
			
			logger.info("PERMANENTS: "+PERMANENTS);
			
			InputStream inputStreamCSV = null;
			try{
				inputStreamCSV = GoogleImporter.importFromGoogleDrive(config);
			}catch(SocketTimeoutException e){
				logger.info("Error downloading from Google Drive: "+e.getMessage());
			}finally{
				if(inputStreamCSV==null){
					logger.info("Importing CSV from local: "+"Futbol7.csv");
					ClassLoader classLoader = Main.class.getClassLoader();
					inputStreamCSV = classLoader.getResourceAsStream("Futbol7.csv");
				}
			}
			matches = formatCSVdata(inputStreamCSV);
			numMatches = matches.size()-1;
			matches.remove(0);
			logger.info("Num of Matches: "+numMatches);
			logger.info("Matches: "+matches);
			
			fullRanking = getRanking();
			
			results = getResults();
			
			pointsSeries = getPointsSeries();

			File project =  new File("");
			
			String jsonSrcFolder = project.getAbsolutePath()+"\\src\\main\\webapp\\resources\\json\\";
			
			logger.info("Project src json folder: "+jsonSrcFolder);
					
			writeToServer(writeJSONtoFile(jsonSrcFolder,"full.js",jsonToString(fullRanking)),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"pair.js",jsonToString(getPair())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"permanents.js",jsonToString(getRankingPermanents())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"substitutes.js",jsonToString(getRankingSubstitutes())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"vs.js",jsonToString(getVS())),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"pointsSeries.js",jsonToString(pointsSeries)),"/resources/json");
			writeToServer(writeJSONtoFile(jsonSrcFolder,"matches.js",jsonToString(results)),"/resources/json");
			
			writeToServer(new File(project.getAbsolutePath()+"\\src\\main\\webapp\\index.html"),"");
        
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private List<Map<String, Object>> getResults() throws ParseException {
		List<Map<String, Object>> res = new ArrayList<Map<String,Object>>();
		
		for(List<String> match : matches){
			Map<String, Object> formattedMatch = new HashMap<String, Object>();
			
			formattedMatch.put("day", formatter.parse(match.get(0)));
			formattedMatch.put("scoreBlues", match.get(8));
			formattedMatch.put("scoreWhites", match.get(9));
			List<Map<String,String>> teams = new ArrayList<Map<String,String>>();
			for(int i=1;i<8;i++){
				Map<String,String> r = new HashMap<String, String>();
				r.put("blue", match.get(i));
				r.put("white", match.get(i+9));
				teams.add(r);
			}
			formattedMatch.put("data", teams);
			res.add(formattedMatch);
		}
		
		return res;
	}

	private List<Map<String, Object>> getPointsSeries() throws ParseException {
		Date today = new Date();
        List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
        for(String name : players){
        	List<List<Object>> playerData = new ArrayList<List<Object>>();
        	int points = 0;
        	for(List<String> row : matches){
        		if(row.contains(name)){
	        		int i=0;
	        		int gA=0,gB=0;
	        		String date = null;
	        		String colour=null;
	        		for(String cell : row){
	        			if(i==0){date=cell;}
	        			if(i==8){gA=Integer.parseInt(cell);}
	        			if(i==9){gB=Integer.parseInt(cell);}
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
        	
        	//Hack to add current points at current day
        	Integer maxPts = (Integer) playerData.get(playerData.size()-1).get(1);
			List<Object> game = new ArrayList<Object>();
			game.add(today);
			game.add(maxPts);
			playerData.add(game);
        	
        	Map<String, Object> e = new HashMap<String, Object>();
        	e.put("name", name);
        	e.put("data", playerData);
			data.add(e);
        }
		return data;
	}

	private File writeJSONtoFile(String folder, String file, String jsonString) throws IOException {
		String varName = file.split("\\.")[0];
		logger.info("File created: "+folder+file+"; var "+varName+" = "+ jsonString+ ";");
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

	private List<Map<String,String>> getVS() throws java.text.ParseException {
		Map<Set<String>,Integer> vs = new HashMap<Set<String>,Integer>();
		
		for(List<String> row: matches){
			for(int i=1;i<8;i++){
				for(int j=10;j<17;j++){
					Set<String> pair = new HashSet<String>(Arrays.asList(row.get(i), row.get(j)));
					vs.put(pair, ((vs.containsKey(pair))?vs.get(pair):0) + 1);
				}
			}
		}
		
        List<Map<String,String>> data = new ArrayList<Map<String,String>>();
        for(Entry<Set<String>, Integer> row : vs.entrySet()){
        	Map<String, String> e = new HashMap<String, String>();
        	e.put("player1", (String)row.getKey().toArray()[0]);
        	e.put("player2", (String)row.getKey().toArray()[1]);
        	e.put("vs", row.getValue().toString());
			data.add(e);
        }
		return data;
		
	}
	
	private List<Map<String,String>> getPair() throws java.text.ParseException {
		Map<Set<String>,Integer> vs = new HashMap<Set<String>,Integer>();
		
		for(List<String> row: matches){
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
		
        List<Map<String,String>> data = new ArrayList<Map<String,String>>();
        for(Entry<Set<String>, Integer> row : vs.entrySet()){
        	Map<String, String> e = new HashMap<String, String>();
        	e.put("player1", (String)row.getKey().toArray()[0]);
        	e.put("player2", (String)row.getKey().toArray()[1]);
        	e.put("pair", row.getValue().toString());
			data.add(e);
        }
		return data;
		
	}
	
	public String jsonToString(Object o) throws JsonGenerationException, JsonMappingException, IOException{
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(o);
        return jsonInString;
		
	}
	
	public List<Map<String,String>> getRankingPermanents(){

		List<Map<String,String>> permanents = new ArrayList<Map<String,String>>();
		for(Map<String, String> row : fullRanking){
        	if(PERMANENTS.contains(row.get("name")) ){
        		permanents.add(row);
        	}
		}
        return permanents;
        
	}
	
	public List<Map<String,String>> getRankingSubstitutes(){
		List<Map<String,String>> substitutes = new ArrayList<Map<String,String>>();
		for(Map<String, String> row : fullRanking){
        	if(!PERMANENTS.contains(row.get("name")) ){
        		substitutes.add(row);
        	}
		}
        return substitutes;
		
	}
		   

	private List<Map<String, String>> getRanking() throws java.text.ParseException {
        
		Map<String,Integer> points = new HashMap<String,Integer>();
		Map<String,Integer> goalsFor = new HashMap<String,Integer>();
		Map<String,Integer> goalsAgainst = new HashMap<String,Integer>();
		Map<String,Integer> wins = new HashMap<String,Integer>();
		Map<String,Integer> draws = new HashMap<String,Integer>();
		Map<String,Integer> loses = new HashMap<String,Integer>();
		Map<String,Integer> matches = new HashMap<String,Integer>();
        players = getListOfPlayers(points, goalsFor, goalsAgainst, wins, draws, loses, matches);
        logger.info(players.toString());
        List<Map<String,String>> data = new ArrayList<Map<String,String>>();
        for(String name : players){
        	Map<String, String> e = new HashMap<String, String>();
        	e.put("name", name);
        	e.put("points", points.get(name).toString());
        	e.put("goalsFor", goalsFor.get(name).toString());
        	e.put("goalsAgainst", goalsAgainst.get(name).toString());
        	e.put("wins", wins.containsKey(name)?wins.get(name).toString():"0");
        	e.put("draws", draws.containsKey(name)?draws.get(name).toString():"0");
        	e.put("loses", loses.containsKey(name)?loses.get(name).toString():"0");
        	e.put("matches", matches.get(name).toString());
        	e.put("lastMatches", getLastMatches(name));
        	//*(matches.get(name)<(numMatches/3)?0:1.0F) in case of less than 1/3 of total match the avg is 0 or 99
        	boolean valid = matches.get(name)>=(numMatches/3);
        	e.put("pointsAVG", (valid?new Float(points.get(name)*1.0F/matches.get(name)):"").toString());
        	e.put("goalsForAVG", (valid?new Float(goalsFor.get(name)*1.0F/matches.get(name)):"").toString());
        	e.put("goalsAgainstAVG", (valid?new Float(goalsAgainst.get(name)*1.0F/matches.get(name)):"").toString());
			data.add(e);
        }
		return data;
	}

	private String getLastMatches(String name) {
		String ret = "";
		for(List<String> row: matches){
			if(row.contains("Fecha")) continue;
			if(row.contains(name)){
				int i = row.indexOf(name);
				int gFor = ((i<8)?Integer.parseInt(row.get(8)):Integer.parseInt(row.get(9)));
				int gAgainst = ((i<8)?Integer.parseInt(row.get(9)):Integer.parseInt(row.get(8)));
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

	private Set<String> getListOfPlayers(Map<String,Integer> points,
												Map<String,Integer> goalsFor,
												Map<String,Integer> goalsAgainst,
												Map<String,Integer> wins,
												Map<String,Integer> draws,
												Map<String,Integer> loses,
												Map<String,Integer> numMatches) throws java.text.ParseException {
		Set<String> names = new TreeSet<String>();
	
		for(List<String> row: matches){
			if(row.contains("Fecha")) continue;
//			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//			Date date = formatter.parse(row.get(0));
			Integer goalsA = Integer.parseInt(row.get(8));
			Integer goalsB = Integer.parseInt(row.get(9));
			for(int i=0;i<row.size();i++){
				String player = row.get(i);
				if(i!=0&&i!=8&&i!=9&&i!=17){
					names.add(row.get(i));
					int gFor = ((i<8)?goalsA:goalsB);
					int gAgainst = ((i<8)?goalsB:goalsA);
					int pointsM = ((gFor>gAgainst)?3:((gFor<gAgainst)?1:2));
					goalsFor.put(player, (goalsFor.get(player)!=null?goalsFor.get(player):0) + gFor );
					goalsAgainst.put(player, (goalsAgainst.get(player)!=null?goalsAgainst.get(player):0) + gAgainst );
					points.put(player, (points.get(player)!=null?points.get(player):0) + pointsM );
					switch (pointsM) {
		            case 3:  wins.put(player, (wins.get(player)!=null?wins.get(player):0) + 1 );
		                     break;
		            case 2:  draws.put(player, (draws.get(player)!=null?draws.get(player):0) + 1 );
		                     break;
		            case 1:  loses.put(player, (loses.get(player)!=null?loses.get(player):0) + 1 );
		                     break;
					}
					numMatches.put(player, (numMatches.get(player)!=null?numMatches.get(player):0) + 1 );
				}
			}
		}
		return names;
	}

	
}

package com.ppot14.futbol7;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Main {
	
	private static List<String> PERMANENTS = null;
	
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	
	private static Map<String,Object> config = null;
	
	private List<Map<String,String>> fullRanking = null;
	private int numMatches = 0;
	private List<List<String>> matches = null;
	private Set<String> players = null;
    
    public Main(){
    	
		try{
			
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
			
			PERMANENTS = (List<String>) config.get("permanents");
			
			logger.info("PERMANENTS: "+PERMANENTS);
			

			
			InputStream inputStreamCSV = GoogleImporter.importFromGoogleDrive(config);
			if(inputStreamCSV==null){
				logger.info("Importing CSV from local: "+"Futbol7.csv");
				ClassLoader classLoader = Main.class.getClassLoader();
				inputStreamCSV = classLoader.getResourceAsStream("Futbol7.csv");
			}
			matches = formatCSVdata(inputStreamCSV);
			numMatches = matches.size()-1;
			matches.remove(0);
			logger.info("Num of Matches: "+numMatches);
			logger.info("Matches: "+matches);
			
			fullRanking = getRanking(matches);

			File project =  new File("");
			
//			String jsonFolderURL = project+"target"+File.separatorChar+
//					"futbol7-0.0.1-SNAPSHOT"+File.separatorChar+"resources"+File.separatorChar+"json"+File.separatorChar;
			
			String jsonSrcFolder = project.getAbsolutePath()+"\\src\\main\\webapp\\resources\\json\\";
			
			logger.info("Project src json folder: "+jsonSrcFolder);
					
			writeJSONtoFile(jsonSrcFolder,"full.js",getRankingJSON());
			writeJSONtoFile(jsonSrcFolder,"pair.js",getPairJSON());
			writeJSONtoFile(jsonSrcFolder,"permanents.js",getRankingPermanentsJSON());
			writeJSONtoFile(jsonSrcFolder,"substitutes.js",getRankingSubstitutesJSON());
			writeJSONtoFile(jsonSrcFolder,"vs.js",getVSJSON());
			
			logger.info("DONE.");
        
		}catch(Exception e){
			e.printStackTrace();
		}
    	
    }

	private void writeJSONtoFile(String folder, String file, String jsonString) throws IOException {
		String varName = file.split("\\.")[0];
		logger.info("varName: "+varName);
		List<String> lines = Arrays.asList("var "+varName+" = ", jsonString, ";");
		logger.info("folder+file: "+folder+file);
		Path path = Paths.get(folder+file);
		Files.write(path, lines, Charset.forName("UTF-8"));
		
	}

	private List<Map<String,String>> getVS(List<List<String>> matches2) throws java.text.ParseException {
		Map<Set<String>,Integer> vs = new HashMap<Set<String>,Integer>();
		
		for(List<String> row: matches2){
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
	
	private List<Map<String,String>> getPair(List<List<String>> matches2) throws java.text.ParseException {
		Map<Set<String>,Integer> vs = new HashMap<Set<String>,Integer>();
		
		for(List<String> row: matches2){
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

	public static void main(String[] args) throws Exception {
		new Main();
	}
	
	public String getVSJSON() throws java.text.ParseException, JsonGenerationException, JsonMappingException, IOException {
		List<Map<String,String>> vs = getVS(matches);
		logger.info("VS: "+vs.toString());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(vs);
        return jsonInString;
	}
	
	public String getPairJSON() throws java.text.ParseException, JsonGenerationException, JsonMappingException, IOException {
		List<Map<String,String>> vs = getPair(matches);
		logger.info("Pair: "+vs.toString());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(vs);
        return jsonInString;
	}
	
	public String getRankingJSON() throws JsonGenerationException, JsonMappingException, IOException{
		logger.info("Full: "+fullRanking.toString());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(fullRanking);
        return jsonInString;
        
	}
	
	public String getRankingPermanentsJSON() throws JsonGenerationException, JsonMappingException, IOException{

		List<Map<String,String>> permanents = new ArrayList<Map<String,String>>();
		for(Map<String, String> row : fullRanking){
        	if(PERMANENTS.contains(row.get("name")) ){
        		permanents.add(row);
        	}
		}
		logger.info("Permanents: "+permanents.toString());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(permanents);
        return jsonInString;
        
	}
	
	public String getRankingSubstitutesJSON() throws JsonGenerationException, JsonMappingException, IOException{
		List<Map<String,String>> substitutes = new ArrayList<Map<String,String>>();
		for(Map<String, String> row : fullRanking){
        	if(!PERMANENTS.contains(row.get("name")) ){
        		substitutes.add(row);
        	}
		}
		logger.info("Substitutes: "+substitutes.toString());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(substitutes);
        return jsonInString;
		
	}
		   

	private List<Map<String, String>> getRanking(List<List<String>> table) throws java.text.ParseException {
        
		Map<String,Integer> points = new HashMap<String,Integer>();
		Map<String,Integer> goalsFor = new HashMap<String,Integer>();
		Map<String,Integer> goalsAgainst = new HashMap<String,Integer>();
		Map<String,Integer> wins = new HashMap<String,Integer>();
		Map<String,Integer> draws = new HashMap<String,Integer>();
		Map<String,Integer> defeats = new HashMap<String,Integer>();
		Map<String,Integer> matches = new HashMap<String,Integer>();
        players = getListOfPlayers(table, points, goalsFor, goalsAgainst, wins, draws, defeats, matches);
        logger.info(players.toString());
        List<Map<String,String>> data = new ArrayList<Map<String,String>>();
        for(String name : players){
//        	if(isPermanent&&PERMANENTS.contains(name) || !isPermanent&&!PERMANENTS.contains(name) ){
	        	Map<String, String> e = new HashMap<String, String>();
	        	e.put("name", name);
	        	e.put("points", points.get(name).toString());
	        	e.put("goalsFor", goalsFor.get(name).toString());
	        	e.put("goalsAgainst", goalsAgainst.get(name).toString());
	        	e.put("wins", wins.containsKey(name)?wins.get(name).toString():"0");
	        	e.put("draws", draws.containsKey(name)?draws.get(name).toString():"0");
	        	e.put("defeats", defeats.containsKey(name)?defeats.get(name).toString():"0");
	        	e.put("matches", matches.get(name).toString());
	        	//*(matches.get(name)<(numMatches/3)?0:1) in case of less than 1/3 of total match is 0
	        	e.put("pointsAVG", new Float(points.get(name)*1.0F/matches.get(name)).toString());
	        	e.put("goalsForAVG", new Float(goalsFor.get(name)*1.0F/matches.get(name)).toString());
	        	e.put("goalsAgainstAVG", new Float(goalsAgainst.get(name)*1.0F/matches.get(name)).toString());
				data.add(e);
//        	}
        }
		return data;
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

	private static Set<String> getListOfPlayers(List<List<String>> table, 
												Map<String,Integer> points,
												Map<String,Integer> goalsFor,
												Map<String,Integer> goalsAgainst,
												Map<String,Integer> wins,
												Map<String,Integer> draws,
												Map<String,Integer> defeats,
												Map<String,Integer> matches) throws java.text.ParseException {
		Set<String> names = new TreeSet<String>();
	
		for(List<String> row: table){
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
		            case 1:  defeats.put(player, (defeats.get(player)!=null?defeats.get(player):0) + 1 );
		                     break;
					}
					matches.put(player, (matches.get(player)!=null?matches.get(player):0) + 1 );
				}
			}
		}
		return names;
	}

	
}

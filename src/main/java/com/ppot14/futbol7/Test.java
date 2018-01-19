package com.ppot14.futbol7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class Test {
	
	private static final Logger logger = Logger.getLogger(Test.class.getName());
	
	protected static APIUtil a;

	public static void main(String[] args) throws Exception {
		
//		testAPI();
		Map<String,Object> config = DBConnector.getConfig();
		
//		testDownloadSpreadsheet(config);
		
		testUploadFile(config);
		
		System.exit(0);
		
	}
	
	static void testUploadFile(Map<String,Object> config) throws Exception{
		GoogleImporter.uploadToGoogleDrive(config, "C:\\Users\\Pepe\\Google Drive\\Web\\Futbol7\\backup\\dump-2018-01-12.tar.gz", (String)config.get("backup-folder-google-drive-id"));
	}
	
	static void testDownloadSpreadsheet(Map<String,Object> config) throws Exception{
		GoogleImporter.importFromGoogleDrive(config, null);
	}
	
	static void testAPI() throws InterruptedException{
		long startTime = System.currentTimeMillis();
		
		a = new APIUtil(null);
//		m.processData(true);
//		m.writeToFileAndServer();
		

//		try{
//			GoogleImporter.testing(m.getConfig());
//		}catch(SocketTimeoutException e){
//			logger.severe("Error testing Google Drive: "+e.getMessage());
//		}
		ObjectMapper mapper = new ObjectMapper(); 
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("date", 1509450678000l);
		map.put("season", "9999-9999");
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
		for(int i=0;i<13;i++){
			Map<String,Object> score1 = new HashMap<String, Object>();
			score1.put("voter", "Tu");
			score1.put("voted", "Yo"+i);
			score1.put("score", 10);
			score1.put("comment", "Hola"+i);
			list.add(score1);
		}
		map.put("scores", list);
		JsonNode jsonNode = mapper.valueToTree(map);
		
//		Main m = new Main();
		Worker worker1 = new Test().new Worker(jsonNode);
		Worker worker2 = new Test().new Worker(jsonNode);
		Worker worker3 = new Test().new Worker(jsonNode);
        worker1.start();
        worker2.start();
        worker3.start();
        worker1.join();
        worker2.join();
        worker3.join();
        
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);  
		
		logger.info("Futbol7 executed in "+duration/1000+"s");
	}
	
	class Worker extends Thread  {
		
		JsonNode jsonNode;

		public Worker(JsonNode jsonNode) {
			this.jsonNode = jsonNode;
		}

		@Override
	    public void run() {
			logger.info("run() START");
			a.savePolling(jsonNode);
			logger.info("run() END");
	    }

	}
}

package com.ppot14.futbol7;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class Main {
	
	private static final Logger logger = Logger.getLogger(Main.class.getName());


	public static void main(String[] args) throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		APIUtil m = new APIUtil("config.json");
//		m.processData(true);
//		m.writeToFileAndServer();
		

		try{
			GoogleImporter.testing(m.getConfig());
		}catch(SocketTimeoutException e){
			logger.severe("Error testing Google Drive: "+e.getMessage());
		}
		
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);  
		
		logger.info("Futbol7 executed in "+duration/1000+"s");
	}
	
}

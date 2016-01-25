package com.ppot14.futbol7;

import java.util.logging.Logger;

public class Main {
	
	private static final Logger logger = Logger.getLogger(Main.class.getName());


	public static void main(String[] args) throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		APIUtil m = new APIUtil();
		m.run(true);
		m.writeToFileAndServer();
		
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);  
		
		logger.info("Futbol7 executed in "+duration/1000+"s");
	}
	
}

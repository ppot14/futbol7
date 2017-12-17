package com.ppot14.futbol7.servlet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;

import com.ppot14.futbol7.APIUtil;
import com.ppot14.futbol7.DBConnector;

/**
 * Servlet
 */
public class Futbol7Servlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Futbol7Servlet.class.getName());
	private static String configurationFile = null;
	private static Map<String,Object> config = null;
	private static APIUtil api;
    
    /**
     * 
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
    	if(configurationFile==null) configurationFile = config.getServletContext().getInitParameter("configurationFile");
    	refreshConfig();
    	if(api==null){ api = new APIUtil(getConfig()); }
    }
    
    /**
	 * @return the api
	 */
	public static APIUtil getApi() {
		return api;
	}

	public static Map<String,Object> getConfig(){
    	return config;
    }
    
    public static void refreshConfig(){
    	refreshConfig(configurationFile);
    }
    
    public static synchronized void refreshConfig(String propFileName){

		config = null;

		try {
			config = DBConnector.getConfig();
			if(config==null){
		        ObjectMapper mapper = new ObjectMapper();
				InputStream inputStream = APIUtil.class.getClassLoader().getResourceAsStream(propFileName);
				if (inputStream != null) {
					config = mapper.readValue(inputStream, Map.class);
					inputStream.close();
				} else {
					throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    protected String getBody(final HttpServletRequest request) throws IOException {

		final StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		
		try {
		        
		    bufferedReader = request.getReader();
		    char[] charBuffer = new char[128];
		    int bytesRead = -1;
		        
		    while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
				stringBuilder.append(charBuffer, 0, bytesRead);
		    }
		
		} catch (IOException ex) {
			
			logger.log(Level.SEVERE, "Error getting body from the request", ex);
		    throw ex;
		
		} finally {
		
		    if (bufferedReader != null) {
				try {
				    bufferedReader.close();
				} catch (IOException ex) {
				    throw ex;
				}
		    }
		}
	
		return java.net.URLDecoder.decode(stringBuilder.toString(), "UTF-8");
    }	
}

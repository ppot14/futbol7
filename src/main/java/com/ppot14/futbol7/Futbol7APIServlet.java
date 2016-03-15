package com.ppot14.futbol7;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.text.ParseException;
import java.util.logging.*;

/**
 * Servlet implementation class ServletTest
 */
public class Futbol7APIServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Futbol7APIServlet.class.getName());

	private APIUtil api;
    /**
     * Default constructor. 
     */
    public Futbol7APIServlet() {
    	logger.info("Futbol7APIServlet created");
    }
    
    /**
     * 
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
    	logger.info("Futbol7APIServlet init");
    	
    	String configurationFile = config.getServletContext().getInitParameter("configurationFile");
    	
    	api = new APIUtil(configurationFile);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		final String requestPath = request.getRequestURI();
    	logger.info("Futbol7APIServlet doGet RequestURI: "+requestPath+", New session: "+session.isNew()+", Session: "+session.getId());
    	ObjectMapper mapper = new ObjectMapper();
    	
    	Object reply = null;
    	boolean processed = api.processData(requestPath.contains("/api/refresh.json"));
    	try {
			if(requestPath.contains("/api/refresh.json")){
				reply = processed; }
			if(requestPath.contains("/api/full.json")){
	    		reply = api.getFullRanking(); }
			if(requestPath.contains("/api/pair.json")){
	    		reply = api.getPair(); }
			if(requestPath.contains("/api/permanents.json")){
	    		reply = api.getRankingPermanents(); }
			if(requestPath.contains("/api/substitutes.json")){
	    		reply = api.getRankingSubstitutes(); }
			if(requestPath.contains("/api/vs.json")){
	    		reply = api.getVS(); }
			if(requestPath.contains("/api/pointsSeries.json")){
	    		reply = api.getPointsSeries();}
			if(requestPath.contains("/api/matches.json")){
    	    	reply = api.getResults(); }
			if(requestPath.contains("/api/players.json")){
    	    	reply = api.getPlayers(); }
		} catch (ParseException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}

		response.setContentType("application/json");
		response.setStatus(200);
		mapper.writeValue(response.getOutputStream(), reply);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	logger.info("Futbol7APIServlet doPost");
		final String requestPath = request.getRequestURI();
    	ObjectMapper mapper = new ObjectMapper();
		final String requestBody = getBody(request);
		logger.info("requestBody: "+requestBody);
		JsonNode jsonNode = mapper.readTree(requestBody);
    	Object reply = null;
    	
		if(requestPath.contains("/api/comparison.json")){
	    	reply = api.getComparison(jsonNode); }

		response.setContentType("application/json");
		response.setStatus(200);
		mapper.writeValue(response.getOutputStream(), reply);
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

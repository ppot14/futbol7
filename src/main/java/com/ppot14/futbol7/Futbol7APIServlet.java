package com.ppot14.futbol7;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    	logger.fine("Futbol7APIServlet created");
    }
    
    /**
     * 
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
    	logger.fine("Futbol7APIServlet init");
    	
    	String configurationFile = config.getServletContext().getInitParameter("configurationFile");
    	
    	api = new APIUtil(configurationFile);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		HttpSession session = request.getSession();
		final String requestPath = request.getRequestURI();
    	logger.fine("RequestURI: "+requestPath);
    	ObjectMapper mapper = new ObjectMapper();
    	
    	Object reply = null;
    	boolean processed = api.processData(requestPath.contains("/api/refresh.json"));
    	try {
			if(requestPath.contains("/api/refresh.json")){
				reply = processed; }
			else if(requestPath.contains("/api/full.json")){
	    		reply = api.getFullRanking(); }
			else if(requestPath.contains("/api/pair.json")){
	    		reply = api.getPair(); }
			else if(requestPath.contains("/api/permanents.json")){
	    		reply = api.getRankingPermanents(); }
			else if(requestPath.contains("/api/substitutes.json")){
	    		reply = api.getRankingSubstitutes(); }
			else if(requestPath.contains("/api/vs.json")){
	    		reply = api.getVS(); }
			else if(requestPath.contains("/api/pointsSeries.json")){
	    		reply = api.getPointsSeries();}
			else if(requestPath.contains("/api/matches.json")){
    	    	reply = api.getResults(); }
			else if(requestPath.contains("/api/players.json")){
    	    	reply = api.getPlayers(); }
			else if(requestPath.contains("/api/options.json")){
    	    	reply = api.getOptions(); }
			else if(requestPath.contains("/api/scorers.json")){
	    		reply = api.getFullScorers(); }
			else if(requestPath.contains("/api/playersPictures.json")){
	    		reply = api.getPlayersPictures(); }
			else{
				logger.warning("Request path not found: "+requestPath);
			}
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
		final String requestPath = request.getRequestURI();
    	logger.info("RequestURI: "+requestPath);
    	ObjectMapper mapper = new ObjectMapper();
		final String requestBody = getBody(request);
		logger.fine("requestBody: "+requestBody);
		JsonNode jsonNode = mapper.readTree(requestBody);
    	Object reply = null;

    	try {
			if(requestPath.contains("/api/comparison.json")){
		    	reply = api.getComparison(jsonNode);
			}else if(requestPath.contains("/api/player.json")){
				reply = api.getPlayer(jsonNode);
			}else if(requestPath.contains("/api/player-has-voted.json")){
				reply = api.hasVoted(jsonNode);
			}else if(requestPath.contains("/api/last-match-result.json")){
				reply = api.getLastMatchResult(jsonNode);
			}else if(requestPath.contains("/api/match-scorers.json")){
				reply = api.getMatchScorers(jsonNode);
			}else if(requestPath.contains("/api/save-polling.json")){
				reply = api.savePolling(jsonNode);
			}else{
				logger.warning("Request path not found: "+requestPath);
			}
		} catch (Exception e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}

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

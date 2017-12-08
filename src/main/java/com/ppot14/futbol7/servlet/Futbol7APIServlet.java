package com.ppot14.futbol7.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bson.Document;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.ppot14.futbol7.APIUtil;

import java.text.ParseException;
import java.util.Map;
import java.util.logging.*;

/**
 * Servlet implementation
 */
public class Futbol7APIServlet extends Futbol7Servlet {
	
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
		long startTime = System.currentTimeMillis();
		
//		super.init(config);
    	
    	api = new APIUtil(super.getConfig());

		long endTime = System.currentTimeMillis();
    	logger.info("Futbol7APIServlet init ("+(endTime - startTime)+"ms)");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		long startTime = System.currentTimeMillis();
		final String requestPath = request.getRequestURI();
		Map<String, String[]> parameters = request.getParameterMap();
    	ObjectMapper mapper = new ObjectMapper();
    	
    	Object reply = null;
    	boolean refresh = requestPath.contains("api/refresh.request");
    	if(refresh){ refreshConfig(); }
    	boolean processed = api.processData(refresh, getConfig());
    	
    	try {
			if(refresh){
				reply = processed; 
    		}else if(requestPath.contains("api/full.request")){
	    		reply = api.getFullRanking(); 
			}else if(requestPath.contains("api/pair.request")){
	    		reply = api.getPair(); 
			}else if(requestPath.contains("api/permanents.request")){
	    		reply = api.getRankingPermanents(); 
			}else if(requestPath.contains("api/substitutes.request")){
	    		reply = api.getRankingSubstitutes(); 
			}else if(requestPath.contains("api/vs.request")){
	    		reply = api.getVS(); 
			}else if(requestPath.contains("api/pointsSeries.request")){
	    		reply = api.getPointsSeries();
			}else if(requestPath.contains("api/userPointsSeries.request")){
	    		reply = session.getAttribute("user")!=null?api.getPointsSeries((String)((Document)session.getAttribute("user")).get("nameweb")):null;
			}else if(requestPath.contains("api/matches.request")){
    	    	reply = api.getResults(); 
			}else if(requestPath.contains("api/players.request")){
    	    	reply = api.getPlayers(); 
			}else if(requestPath.contains("api/options.request")){
    	    	reply = api.getPermanents(); 
			}else if(requestPath.contains("api/scorers.request")){
	    		reply = api.getFullScorers(); 
			}else if(requestPath.contains("api/playersPictures.request")){
	    		reply = api.getPlayersPictures(); 
			}else if(requestPath.contains("api/logout.request")){
				session.removeAttribute("user");
			}else if(requestPath.contains("api/userStats.request")){
	    		reply = session.getAttribute("user")!=null?api.getUserStats(parameters.get("season")[0],(String)((Document)session.getAttribute("user")).get("nameweb")):null; 
			}else if(requestPath.contains("api/userMatches.request")){
	    		reply = session.getAttribute("user")!=null?api.getUserMatches((String)((Document)session.getAttribute("user")).get("nameweb")):null; 
			}else{
				logger.warning("Request path not found: "+requestPath);
			}
		} catch (ParseException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}

		response.setContentType("application/json");
		response.setStatus(200);
		mapper.writeValue(response.getOutputStream(), reply);
		long endTime = System.currentTimeMillis();
    	logger.info("RequestURI ("+(endTime - startTime)+"ms): "+requestPath);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		long startTime = System.currentTimeMillis();
		final String requestPath = request.getRequestURI();
    	ObjectMapper mapper = new ObjectMapper();
		final String requestBody = getBody(request);
		logger.fine("requestBody: "+requestBody);
		JsonNode jsonNode = mapper.readTree(requestBody);
    	Object reply = null;

    	try {
			if(requestPath.contains("api/comparison.request")){
		    	reply = api.getComparison(jsonNode);
			}else if(requestPath.contains("api/login.request")){
				Object o = api.login(jsonNode);
				if(o!=null){
					session.setAttribute("user", o);
				}
				reply = o;
			}else if(requestPath.contains("api/player-has-voted.request")){
				reply = api.hasVoted(jsonNode);
			}else if(requestPath.contains("api/last-match-result.request")){
				reply = api.getLastMatchResult(jsonNode);
			}else if(requestPath.contains("api/match-scorers.request")){
				reply = api.getMatchScorers(jsonNode);
			}else if(requestPath.contains("api/save-polling.request")){
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
		long endTime = System.currentTimeMillis();
    	logger.info("RequestURI ("+(endTime - startTime)+"ms): "+requestPath);
	}
}

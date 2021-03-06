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
		
		super.init(config);

		long endTime = System.currentTimeMillis();
    	logger.info("Futbol7APIServlet init ("+(endTime - startTime)+"ms)");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		HttpSession session = request.getSession(true);
//		String user = session.getAttribute("user")!=null?(String)((Document)session.getAttribute("user")).get("nameweb"):null;
		final String requestPath = request.getRequestURI();
		Map<String, String[]> parameters = request.getParameterMap();
    	ObjectMapper mapper = new ObjectMapper();
    	
    	Object reply = null;
    	boolean refresh = requestPath.contains("api/refresh.request");
    	if(refresh){ refreshConfig(); }
    	boolean processed = getApi().processData(refresh, getConfig());
    	
		if(refresh){
			reply = processed; 
		}else if(requestPath.contains("api/logout.request")){
			session.removeAttribute("user");
		}else if(requestPath.contains("api/options.request")){
			reply = getApi().getPermanents(getConfig());
		}else if(requestPath.contains("api/playersPictures.request")){
			reply = getApi().getPlayersPictures();
		}else{
			logger.warning("Request path not found: "+requestPath);
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
		long startTime = System.currentTimeMillis();
		HttpSession session = request.getSession(true);
		final String requestPath = request.getRequestURI();
    	ObjectMapper mapper = new ObjectMapper();
		final String requestBody = getBody(request);
		logger.fine("requestBody: "+requestBody);
		JsonNode jsonNode = mapper.readTree(requestBody);
    	Object reply = null;

    	try {
			if(requestPath.contains("api/comparison.request")){
		    	reply = getApi().getComparison(jsonNode);
			}else if(requestPath.contains("api/login.request")){
				Object o = getApi().login(jsonNode);
				if(o!=null){
					session.setAttribute("user", o);
					logger.info("User set in session: "+session.getId());
				}
				reply = o;
			}else if(requestPath.contains("api/player-has-voted.request")){
				reply = getApi().hasVoted(jsonNode);
			}else if(requestPath.contains("api/last-match-result.request")){
				reply = getApi().getLastMatchResult(jsonNode);
			}else if(requestPath.contains("api/match-scorers.request")){
				reply = getApi().getMatchScorers(jsonNode);
			}else if(requestPath.contains("api/save-polling.request")){
				reply = getApi().savePolling(jsonNode);
			}else if(requestPath.contains("api/get-mvps.request")){
				String seasonName = jsonNode.get("season").asText();
				reply = getApi().getMVPs(seasonName);
			}else if(requestPath.contains("api/fullRanking.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getFullRanking(seasonName); 
			}else if(requestPath.contains("api/permanentsRanking.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getRankingPermanents(seasonName); 
			}else if(requestPath.contains("api/substitutesRanking.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getRankingSubstitutes(seasonName); 
			}else if(requestPath.contains("api/vs.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getVS(seasonName); 
			}else if(requestPath.contains("api/pair.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getPair(seasonName);
			}else if(requestPath.contains("api/scorers.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getFullScorers(seasonName);
			}else if(requestPath.contains("api/matches.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getResults(seasonName); 
			}else if(requestPath.contains("api/players.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getPlayers(seasonName);
			}else if(requestPath.contains("api/mvps-by-players.request")){
				String seasonName = jsonNode.get("season").asText();
				reply =getApi().getMVPsByPlayers(seasonName);
			}else if(requestPath.contains("api/userMatches.request")){
				String seasonName = jsonNode.get("season").asText();
				String player = jsonNode.get("player").asText();
				reply =getApi().getUserMatches(seasonName, player ) ;
			}else if(requestPath.contains("api/userStats.request")){
				String seasonName = jsonNode.get("season").asText();
				String player = jsonNode.get("player").asText();
	    		reply = getApi().getUserStats(seasonName,player);
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

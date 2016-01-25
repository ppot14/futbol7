package com.ppot14.futbol7;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
    	api.run(false);
    	try {
			if(requestPath.contains("/futbol7/api/full.json")){
	    		reply = api.getFullRanking(); }
			if(requestPath.contains("/futbol7/api/pair.json")){
	    		reply = api.getPair(); }
			if(requestPath.contains("/futbol7/api/permanents.json")){
	    		reply = api.getRankingPermanents(); }
			if(requestPath.contains("/futbol7/api/substitutes.json")){
	    		reply = api.getRankingSubstitutes(); }
			if(requestPath.contains("/futbol7/api/vs.json")){
	    		reply = api.getVS(); }
			if(requestPath.contains("/futbol7/api/pointsSeries.json")){
	    		reply = api.getPointsSeries();}
			if(requestPath.contains("/futbol7/api/matches.json")){
    	    		reply = api.getResults(); }
		} catch (ParseException e) {
			logger.severe(e.getMessage());
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
	}

}

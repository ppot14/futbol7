package com.ppot14.futbol7.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bson.Document;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Servlet implementation class ServletTest
 */
public class Futbol7HTMLServlet extends Futbol7Servlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(Futbol7HTMLServlet.class.getName());

    /**
     * Default constructor. 
     */
    public Futbol7HTMLServlet() {
    	LOG.fine("Futbol7HTMLServlet created");
    }
    
    /**
     * 
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
		long startTime = System.currentTimeMillis();
		
		super.init(config);

		long endTime = System.currentTimeMillis();
    	LOG.info("Futbol7HTMLServlet init ("+(endTime - startTime)+"ms)");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		HttpSession session = request.getSession(true);
		String user = session.getAttribute("user")!=null?(String)((Document)session.getAttribute("user")).get("nameweb"):null;
		ObjectMapper mapper = new ObjectMapper();
		getApi().processData(false, getConfig());
		
		request.setAttribute("minimized", getConfig().get("minimized"));
		request.setAttribute("production", getConfig().get("production"));
		request.setAttribute("options", mapper.writeValueAsString(getApi().getPermanents())); //Only Permanents for now
		
		String requestPath = request.getServletPath();
		if("".equals(requestPath)){
			request.setAttribute("fullRanking", mapper.writeValueAsString(getApi().getFullRanking()) ); 
			request.setAttribute("permanentsRanking", mapper.writeValueAsString(getApi().getRankingPermanents()) ); 
			request.setAttribute("substitutesRanking", mapper.writeValueAsString(getApi().getRankingSubstitutes()) );
			request.setAttribute("playersPictures", mapper.writeValueAsString(getApi().getPlayersPictures()) );
			request.setAttribute("vs", mapper.writeValueAsString(getApi().getVS()) ); 
			request.setAttribute("pair", mapper.writeValueAsString(getApi().getPair()) );
			request.setAttribute("scorers", mapper.writeValueAsString(getApi().getFullScorers()) );
			request.setAttribute("matches", mapper.writeValueAsString(getApi().getResults()) ); 
			request.setAttribute("players", mapper.writeValueAsString(getApi().getPlayers()) );
			request.setAttribute("userPointsSeries", mapper.writeValueAsString(getApi().getPointsSeries()) );
		}else if("/me".equals(requestPath)){
			request.setAttribute("matches", mapper.writeValueAsString(getApi().getResults()) );
			request.setAttribute("userMatches", mapper.writeValueAsString(user!=null? getApi().getUserMatches(user): null ));
			request.setAttribute("userPointsSeries", mapper.writeValueAsString(user!=null? getApi().getPointsSeries(user): null ));
		}
		requestPath = requestPath.length() > 1 ? requestPath + ".jsp" : "/index.jsp";
		
		long endTime = System.currentTimeMillis();
    	LOG.info("RequestURI ("+(endTime - startTime)+"ms): "+requestPath+" ("+(user!=null?user:"")+")");
    	request.getRequestDispatcher(requestPath).forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		HttpSession session = request.getSession(true);
		final String requestPath = request.getServletPath().replace(".html", ".jsp");
		long endTime = System.currentTimeMillis();
    	LOG.info("RequestURI ("+(endTime - startTime)+"ms): "+requestPath);
    	request.getRequestDispatcher(requestPath).forward(request, response);
	}

}

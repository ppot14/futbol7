package com.ppot14.futbol7.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ServletTest
 */
public class Futbol7HTMLServlet extends Futbol7Servlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Futbol7HTMLServlet.class.getName());

    /**
     * Default constructor. 
     */
    public Futbol7HTMLServlet() {
    	logger.fine("Futbol7HTMLServlet created");
    }
    
    /**
     * 
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
		long startTime = System.currentTimeMillis();
		
		super.init(config);

		long endTime = System.currentTimeMillis();
    	logger.info("Futbol7HTMLServlet init ("+(endTime - startTime)+"ms)");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		request.setAttribute("minimized", super.getConfig().get("minimized"));
		request.setAttribute("production", super.getConfig().get("production"));
		String requestPath = request.getServletPath().replace(".html", ".jsp");
		requestPath = requestPath.length() > 1 ? requestPath + ".jsp" : "/index.jsp";
		long endTime = System.currentTimeMillis();
    	logger.info("RequestURI ("+(endTime - startTime)+"ms): "+requestPath);
    	request.getRequestDispatcher(requestPath).forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		final String requestPath = request.getServletPath().replace(".html", ".jsp");
		long endTime = System.currentTimeMillis();
    	logger.info("RequestURI ("+(endTime - startTime)+"ms): "+requestPath);
    	request.getRequestDispatcher(requestPath).forward(request, response);
	}

}

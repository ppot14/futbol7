package com.ppot14.futbol7.servlet;

import com.ppot14.futbol7.DBConnector;
import com.ppot14.futbol7.GoogleImporter;
import org.bson.Document;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Servlet implementation class ServletTest
 */
@MultipartConfig
public class Futbol7UploadServlet extends Futbol7Servlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(Futbol7UploadServlet.class.getName());

    /**
     * Default constructor.
     */
    public Futbol7UploadServlet() {
    	LOG.fine("Futbol7UploadServlet created");
    }
    
    /**
     * 
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
		long startTime = System.currentTimeMillis();
		
		super.init(config);

		long endTime = System.currentTimeMillis();
    	LOG.info("Futbol7UploadServlet init ("+(endTime - startTime)+"ms)");
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		HttpSession session = request.getSession(true);
		Object userObj = session.getAttribute("user");
		Document user = (Document) userObj;
		String playerLogin = user.getString("name");
		String userType = user.getString("usertype");
		Map<String,Object> reply = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		Part filePart = request.getPart("file");
		String player = request.getParameter("player"); // Retrieves <input type="file" name="file">
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
		InputStream fileContent = filePart.getInputStream();
		final String requestPath = request.getServletPath().replace(".html", ".jsp");
		if(requestPath.contains("avatar.upload")) {
			if(playerLogin.equals(player) || userType.equals("admin")) {
				try {
					String newImageURL = GoogleImporter.uploadObject(getConfig(), player, fileName, fileContent);
					reply.put("imageURL", newImageURL);
					DBConnector.setPlayerPicture(player, newImageURL);
				} catch (Exception e) {
					reply.put("error", e.getMessage());
				}
			}else{
				reply.put("error", "Permission denied for player: "+playerLogin);
			}
		}

		response.setContentType("application/json");
		response.setStatus(200);
		mapper.writeValue(response.getOutputStream(), reply);
		long endTime = System.currentTimeMillis();
    	LOG.info("RequestURI ("+(endTime - startTime)+"ms): "+requestPath);
    	//request.getRequestDispatcher(requestPath).forward(request, response);
	}

}

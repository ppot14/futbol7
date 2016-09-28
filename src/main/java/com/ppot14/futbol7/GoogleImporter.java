package com.ppot14.futbol7;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;


public class GoogleImporter {
	
	private static final Logger logger = Logger.getLogger(GoogleImporter.class.getName());
	
	private static final String APPLICATION_NAME = "futbol7";
	
//	private static final java.io.File DATA_STORE_DIR = new java.io.File(
//	        System.getProperty("user.home"), ".credentials/drive-java-futbol7");
	
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,DriveScopes.DRIVE_READONLY,DriveScopes.DRIVE_FILE);
	
//	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	
	private static String SERVICE_ACCOUNT_EMAIL;
	
	private static HttpTransport HTTP_TRANSPORT;
	
//	private static FileDataStoreFactory DATA_STORE_FACTORY;
	
	private static java.io.File p12;
	
//	private static GoogleClientSecrets clientSecrets;
	
	static {
	  try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
	  } catch (Throwable t) {
	      t.printStackTrace();
	  }
	}	

	public static java.io.File testing(Map<String,Object> config) throws Exception{
		
//		String apiProjectJson = (String) config.get("api-project-json");
		String apiProjectP12 = (String) config.get("api-project-p12");
//		String clientSecretJson = (String) config.get("client-secret-json");
		String spreadsheetGoogleDriveId = (String) config.get("spreadsheet-google-drive-id");
		
		ClassLoader classLoader = GoogleImporter.class.getClassLoader();
		//API-Project-77b28b0ca141.json
		//client_secret_517911210517.apps.googleusercontent.com.json
//		InputStream inAPIProjectJson = classLoader.getResourceAsStream(apiProjectJson);
		URL url = classLoader.getResource(apiProjectP12);
		p12 = new java.io.File(url.toURI());
//		InputStream inClientSecretJson = classLoader.getResourceAsStream(clientSecretJson);		
//		clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inClientSecretJson));	
//		ObjectMapper mapper = new ObjectMapper();
//		Map<String,Object> inAPIProject = mapper.readValue(inAPIProjectJson, Map.class);
		SERVICE_ACCOUNT_EMAIL = (String) config.get("client_email");
//		String clientId = ((GoogleClientSecrets.Details) clientSecrets.get("web")).getClientId() ;
//		logger.info("clientId: "+clientId);
		logger.info("serviceAccountEmail: "+SERVICE_ACCOUNT_EMAIL);
        
        Credential credential = getGoogleCredential(true);
		
        Drive driveService = new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        List<File> result = new ArrayList<File>();
        
        try{
	        File spreadSheetMatchesFromGoogleDrive = driveService.files().get(spreadsheetGoogleDriveId).execute();
	        if(spreadSheetMatchesFromGoogleDrive!=null){
	          logger.info(spreadSheetMatchesFromGoogleDrive.getName());
	          logger.info(spreadSheetMatchesFromGoogleDrive.getId());
	        }
        }catch(Exception e){
    		logger.severe("Error getting file: "+spreadsheetGoogleDriveId);
        }

        FileList files = driveService.files().list().execute();
        result.addAll(files.getFiles());
        List<List<String>> matches = null;
        for(File f : result){
        	Map<String,String> appProps = f.getProperties();
        	logger.info("File '"+f.getName()+"', AppProperties: "+appProps);
        	if("Futbol7.csv".equals(f.getOriginalFilename())){
        		InputStream is = downloadFile(driveService, f);
        		if(is!=null){
        			matches = APIUtil.formatCSVdata(is);
            		break;
        		}
        	}else if(spreadsheetGoogleDriveId.equals(f.getId())){
            	logger.info("Downloading: "+spreadsheetGoogleDriveId);
//            		https://developers.google.com/drive/v3/web/manage-downloads
            		InputStream is = driveService.files().export(spreadsheetGoogleDriveId, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").executeAsInputStream();
	        		if(is!=null){
//	        			matches = APIUtil.formatPOIdata(is);
	            		break;
	        		}
        	}
        }
        logger.info("Matches from google drive csv: "+matches);

//      credential.refreshToken();
      
		SpreadsheetService service = new SpreadsheetService(APPLICATION_NAME);
		service.setProtocolVersion(SpreadsheetService.Versions.V2);
		service.setOAuth2Credentials(credential);
//		service.setUserCredentials((String) config.get("client_email"), (String) config.get("private_key_id"));

		logger.info("SpreadsheetService ProtocolVersion: "+service.getProtocolVersion().getVersionString());
		
		URL urlFeed = FeedURLFactory.getDefault().getWorksheetFeedUrl(spreadsheetGoogleDriveId, "private", "full");

		logger.info("WorksheetFeedUrl: " +urlFeed.toString());
		
//		SpreadsheetEntry spreadsheetEntry = service.getEntry(urlFeed, SpreadsheetEntry.class);
//		WorksheetEntry worksheetEntry = spreadsheetEntry.getDefaultWorksheet();
//		logger.info(worksheetEntry.getTitle().toString());
		
//	    SpreadsheetFeed spreadsheetfeed = service.getFeed(urlFeed, SpreadsheetFeed.class);
		WorksheetFeed worksheetfeed = service.getFeed(urlFeed, WorksheetFeed.class);
	    List<WorksheetEntry> spreadsheets = worksheetfeed.getEntries();
	    
	    for (WorksheetEntry spreadsheet : spreadsheets) {
	    	logger.info(spreadsheet.getTitle().getPlainText());
	    }
		
		return null;
	}
	
	public static InputStream importFromGoogleDrive(Map<String,Object> config, String format) throws Exception{
		
//		String apiProjectJson = (String) config.get("api-project-json");
		String apiProjectP12 = (String) config.get("api-project-p12");
//		String clientSecretJson = (String) config.get("client-secret-json");
		String spreadsheetGoogleDriveId = (String) config.get("spreadsheet-google-drive-id");
		
		ClassLoader classLoader = GoogleImporter.class.getClassLoader();
//		InputStream inAPIProjectJson = classLoader.getResourceAsStream(apiProjectJson);
		URL url = classLoader.getResource(apiProjectP12);
		p12 = new java.io.File(url.toURI());
//		InputStream inClientSecretJson = classLoader.getResourceAsStream(clientSecretJson);		
//		clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inClientSecretJson));	
		ObjectMapper mapper = new ObjectMapper();
//		Map<String,Object> inAPIProject = mapper.readValue(inAPIProjectJson, Map.class);
		SERVICE_ACCOUNT_EMAIL = (String) config.get("client_email");
		logger.info("serviceAccountEmail: "+SERVICE_ACCOUNT_EMAIL);
        
        Credential credential = getGoogleCredential(true);
		
        Drive driveService = new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        File spreadSheetMatchesFromGoogleDrive = driveService.files().get(spreadsheetGoogleDriveId).execute();
        if(spreadSheetMatchesFromGoogleDrive!=null){
          logger.info("Downloading: "+spreadSheetMatchesFromGoogleDrive.getName()+" from Google Drive");
        }

//		https://developers.google.com/drive/v3/web/manage-downloads	
        return driveService.files().export(spreadsheetGoogleDriveId, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").executeAsInputStream();

	}

	private static Credential getGoogleCredential(boolean option) throws Exception {
		
		if(option){
	        
			GoogleCredential credentialBuilder = new GoogleCredential.Builder()
			    .setTransport(HTTP_TRANSPORT)
			    .setJsonFactory(JSON_FACTORY)
			    .setServiceAccountScopes(SCOPES)
				.setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
//				.setServiceAccountUser(SERVICE_ACCOUNT_EMAIL)
//				.setServiceAccountPrivateKeyId((String) inAPIProject.get("private_key"))
				.setServiceAccountPrivateKeyFromP12File(p12)
			    .build();
			
			return credentialBuilder;
			
		}
//		else{
//		
//			GoogleAuthorizationCodeFlow flow =
//	            new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//	                .setDataStoreFactory(DATA_STORE_FACTORY)
//	                .setAccessType("offline")
//	                .setApprovalPrompt("force")
//	                .build();
//			
//	        Credential credentialAuthorizationCodeInstalledApp = new AuthorizationCodeInstalledApp(
//	            flow, new LocalServerReceiver()).authorize("user");
//			
//			return credentialAuthorizationCodeInstalledApp;
//        
//		}
		return null;
	}



	private static InputStream downloadFile(Drive service, File file) {
  	  	InputStream ret = null;
  	  
	    if (file.getWebContentLink() != null && file.getWebContentLink().length() > 0) {
	      try {
	        HttpResponse resp = service.getRequestFactory().buildGetRequest(
	        		new GenericUrl(file.getWebContentLink()))
	                	.execute();
	        ret = resp.getContent();
	      } catch (IOException e) {
			logger.warning("Error downloading file from Google Drive: "+e.getMessage());
	      }
	    }

        if(ret==null){
  	      try {
        	ret = service.files().get(file.getId()).executeMediaAsInputStream();
	      } catch (IOException e) {
			logger.warning("Error downloading file from Google Drive: "+e.getMessage());
	      }
        }
        
    	if(ret==null){
    		logger.severe("Error downloading file from Google Drive, Stream: "+ret);
	        return null;
    	}

    	return ret;
	  }
}

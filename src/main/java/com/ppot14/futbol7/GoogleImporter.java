package com.ppot14.futbol7;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;


public class GoogleImporter {
	
	private static final Logger logger = Logger.getLogger(GoogleImporter.class.getName());
	
	private static final String APPLICATION_NAME = "futbol7";
	
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,DriveScopes.DRIVE_READONLY,DriveScopes.DRIVE_FILE);
	
	private static String SERVICE_ACCOUNT_EMAIL;
	
	private static HttpTransport HTTP_TRANSPORT;
	
	private static java.io.File p12;
	
	static {
	  try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	  } catch (Throwable t) {
	      t.printStackTrace();
	  }
	}
	
	public static InputStream importFromGoogleDrive(Map<String,Object> config, String format) throws Exception{
		
		String spreadsheetGoogleDriveId = (String) config.get("spreadsheet-google-drive-id");
        
        Credential credential = getGoogleCredential(config, true);
		
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
	

	public static void main(String[] args) throws Exception {

		if("upload".equals(args[0])){
			String path = args[1];
			Map<String,Object> config = DBConnector.getConfig();
			GoogleImporter.uploadToGoogleDrive(config, path, (String)config.get("backup-folder-google-drive-id"));
		}
	
	}
	
	public static boolean uploadToGoogleDrive(Map<String,Object> config, String path, String targetFolderId) throws Exception{
        
        Credential credential = getGoogleCredential(config, true);
		
        Drive driveService = new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        java.io.File sourceFile = new java.io.File(path);
        File fileMetadata = new File();
        fileMetadata.setName(sourceFile.getName());
        fileMetadata.setParents(Collections.singletonList(targetFolderId));
        FileContent mediaContent = new FileContent(Files.probeContentType(sourceFile.toPath()), sourceFile);
        File file = driveService.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
        System.out.println("Uploaded: "+file.getId());

        return file.getId()!=null;
	}	

	private static Credential getGoogleCredential(Map<String,Object> config, boolean option) throws Exception {
		
		String apiProjectP12 = (String) config.get("api-project-p12");
		
		ClassLoader classLoader = GoogleImporter.class.getClassLoader();
		URL url = classLoader.getResource(apiProjectP12);
		p12 = new java.io.File(url.toURI());
		SERVICE_ACCOUNT_EMAIL = (String) config.get("client_email");
		
		if(option){
	        
			GoogleCredential credentialBuilder = new GoogleCredential.Builder()
			    .setTransport(HTTP_TRANSPORT)
			    .setJsonFactory(JSON_FACTORY)
			    .setServiceAccountScopes(SCOPES)
				.setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
				.setServiceAccountPrivateKeyFromP12File(p12)
			    .build();
			
			return credentialBuilder;
			
		}
		return null;
	}
}

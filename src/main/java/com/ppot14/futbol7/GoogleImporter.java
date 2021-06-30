package com.ppot14.futbol7;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import javax.imageio.ImageIO;


public class GoogleImporter {
	
	private static final Logger logger = Logger.getLogger(GoogleImporter.class.getName());
	
	private static final String APPLICATION_NAME = "futbol7";
	private static final String BUCKET_NAME = "futbol7";
	private static final String GOOGLE_SECRET_JSON = "api-project-517911210517-cfe395aaee80.json";
	
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
		Map<String,Object> config = DBConnector.getConfig();
		if("upload".equals(args[0])){
			String path = args[1];
			GoogleImporter.uploadToGoogleDrive(config, path, (String)config.get("backup-folder-google-drive-id"));
		}else if("list-bucket".equals(args[0])){
			listAvatars(config);
		}else if("upload-storage".equals(args[0])) {
			uploadObject(config, "test.jpg", args[1], new FileInputStream(args[1]));
		}
	}

	public static void listAvatars(Map<String,Object> config) throws Exception {
		Storage storage = StorageOptions.newBuilder().setCredentials(getGoogleCloudCredentials()).build().getService();
		Page<Blob> blobs = storage.list(BUCKET_NAME);
		for (Blob blob : blobs.iterateAll()) {
			System.out.println(blob.getName());
		}
	}
	public static String uploadObject(Map<String,Object> config, String player, String fileName, InputStream fileContent) throws Exception {
		java.io.File tempFile = java.io.File.createTempFile("futbol7", null);
		logger.fine("tempFile: "+tempFile.getAbsolutePath());
		tempFile.deleteOnExit();
		BufferedImage targetImage = resizeImage(ImageIO.read(fileContent), 400, 400);
		String extension = FilenameUtils.getExtension(fileName);
		ImageIO.write(targetImage, extension, tempFile);
		Storage storage = StorageOptions.newBuilder().setCredentials(getGoogleCloudCredentials()).build().getService();
		String objectName = "players/avatars/"+player+"."+extension;
		BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
		Tika tika = new Tika();
		String mimeType = tika.detect(tempFile);
		logger.fine("mimeType: "+mimeType);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType).build();
		Blob result = storage.create(blobInfo, Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
		logger.info("File " + fileName + " uploaded to bucket " + BUCKET_NAME + " as " + result.getName());
		return objectName;
	}

	private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
		logger.info("originalImage " + originalImage.getHeight() + "x" + originalImage.getWidth());
		int side = Math.min(originalImage.getWidth(), originalImage.getHeight());
		int x = (originalImage.getWidth() - side) / 2;
		int y = (originalImage.getHeight() - side) / 2;
		BufferedImage cropped = originalImage.getSubimage(x, y, side, side);
		Image resultingImage = cropped.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
		BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
		logger.fine("outputImage " + outputImage.getHeight() + "x" + outputImage.getWidth());
		return outputImage;
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

	private static GoogleCredentials getGoogleCloudCredentials() throws Exception {
		ClassLoader classLoader = GoogleImporter.class.getClassLoader();
		URL url = classLoader.getResource(GOOGLE_SECRET_JSON);
		logger.info("Credentials in file: " + url.getPath());
		return GoogleCredentials.fromStream(new FileInputStream(url.getPath()))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
	}
}

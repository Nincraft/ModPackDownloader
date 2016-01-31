package com.nincraft.modpackdownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ModPackDownloader {

	private static final String CURSEFORGE_BASE_URL = "http://minecraft.curseforge.com/projects/";
	private static final String COOKIE_TEST_1 = "?cookieTest=1";
	private static final int RETRY_COUNTER = 5;
	static Logger logger = LogManager.getRootLogger();

	public static void main(String[] args) {
		String manifestFile = null;
		String modFolder = null;
		switch (args.length) {
		case 0:
			logger.error("Arguments required: manifest file location, mod download location");
			return;
		case 2:
			manifestFile = args[0];
			modFolder = args[1];
			break;
		default:
			logger.error("Incorrect number of arguments");
			break;
		}
		JSONParser parser = new JSONParser();
		try {
			Long projectID;
			Long fileID;
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(manifestFile));
			JSONArray fileList = (JSONArray) jsonObject.get("files");
			logger.info("Starting download of " + fileList.size() + " mods");
			Iterator iterator = fileList.iterator();
			while (iterator.hasNext()) {
				JSONObject modJson = ((JSONObject) iterator.next());
				projectID = (Long) modJson.get("projectID");
				fileID = (Long) modJson.get("fileID");
				String url = CURSEFORGE_BASE_URL + projectID + COOKIE_TEST_1;
				logger.info(url);
				HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
				con.setInstanceFollowRedirects(false);
				con.connect();
				String location = con.getHeaderField("Location");
				String projectName = location.split("/")[2];
				downloadFile(createCurseDownloadUrl(projectName, fileID), modFolder, projectName);
			}
	
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
		logger.info("Finished downloading mods");
	}

	private static String createCurseDownloadUrl(String projectName, Long fileID) {
		return CURSEFORGE_BASE_URL + projectName + "/files/" + fileID + "/download";
	}

	private static void createFolder(String folder) {
		if (folder != null) {
			File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

	private static void downloadFile(String url, String folder, String projectName) {
		final String jarext = ".jar";
		final String jsonext = ".json";
		createFolder(folder);
		String fileName = projectName + jarext;
		logger.info("Downloading " + url + " to file " + fileName);
		try {
			URL fileThing = new URL(url);

			fileName = getDownloadLocation(url, projectName, jarext, jsonext, fileName);
			ReadableByteChannel rbc = Channels.newChannel(fileThing.openStream());
			FileOutputStream fos;
			if (folder != null) {
				fos = new FileOutputStream(new File(folder + File.separator + fileName));
			} else {
				fos = new FileOutputStream(new File(fileName));
			}
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error("Could not find: " + fileName, e);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static String getDownloadLocation(String url, String projectName, final String jarext, final String jsonext,
			String downloadLocation) throws IOException, MalformedURLException {
		if (downloadLocation.indexOf(jarext) == -1 && downloadLocation.indexOf(jsonext) == -1) {
			HttpURLConnection con = (HttpURLConnection) (new URL(url + COOKIE_TEST_1).openConnection());
			con.setInstanceFollowRedirects(false);
			con.connect();
			String actualURL = con.getURL().toString();
			int retryCount = 0;
			while (con.getResponseCode() != 200 || actualURL.indexOf(jarext) == -1) {
				con = (HttpURLConnection) (new URL(url + COOKIE_TEST_1).openConnection());
				actualURL = con.getURL().toString();
				if (retryCount > RETRY_COUNTER) {
					break;
				}
				retryCount++;
			}

			if (actualURL.substring(actualURL.lastIndexOf('/') + 1).indexOf(jarext) != -1)
				downloadLocation = actualURL.substring(actualURL.lastIndexOf('/') + 1);
			else
				downloadLocation = projectName + jarext;
		}
		return downloadLocation;
	}

}

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
		logger.info("Starting download with parameters: " + manifestFile + ", " + modFolder);
		downloadCurseMods(manifestFile, modFolder);
		downloadThirdPartyMods(manifestFile, modFolder);
		logger.info("Finished downloading mods");
	}

	private static void downloadThirdPartyMods(String manifestFile, String modFolder) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject jsons = (JSONObject) parser.parse(new FileReader(manifestFile));
			JSONArray urlList = (JSONArray) jsons.get("thirdParty");
			if (urlList != null) {
				Iterator iterator = urlList.iterator();
				while (iterator.hasNext()) {
					JSONObject urlJson = (JSONObject) iterator.next();
					String url = (String) urlJson.get("url");
					String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".jar") + 4);
					downloadFile(url, modFolder, fileName);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
	}

	private static void downloadCurseMods(String manifestFile, String modFolder) {
		JSONParser parser = new JSONParser();
		try {
			Long projectID;
			Long fileID;
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(manifestFile));
			JSONArray fileList = (JSONArray) jsonObject.get("curseFiles");
			if(fileList == null){
				fileList = (JSONArray) jsonObject.get("files");
			}
			if (fileList != null) {
				logger.info("Starting download of " + fileList.size() + " mods from Curse");
				Iterator iterator = fileList.iterator();
				while (iterator.hasNext()) {
					JSONObject modJson = (JSONObject) iterator.next();
					projectID = (Long) modJson.get("projectID");
					fileID = (Long) modJson.get("fileID");
					String url = CURSEFORGE_BASE_URL + projectID + COOKIE_TEST_1;
					HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
					con.setInstanceFollowRedirects(false);
					con.connect();
					String location = con.getHeaderField("Location");
					String projectName = location.split("/")[2];
					logger.info("Downloading " + projectName);
					downloadCurseForgeFile(createCurseDownloadUrl(projectName, fileID), modFolder, projectName);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
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

	private static void downloadCurseForgeFile(String url, String folder, String projectName) {
		String fileName = projectName;
		try {
			fileName = getCurseForgeDownloadLocation(url, projectName, fileName);
			downloadFile(url, folder, fileName);
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error("Could not find: " + fileName, e);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static void downloadFile(String url, String folder, String fileName)
			throws MalformedURLException, IOException, FileNotFoundException {
		URL fileThing = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(fileThing.openStream());
		FileOutputStream fos;
		if (folder != null) {
			createFolder(folder);
			fos = new FileOutputStream(new File(folder + File.separator + fileName));
		} else {
			fos = new FileOutputStream(new File(fileName));
		}
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}

	private static String getCurseForgeDownloadLocation(String url, String projectName, String downloadLocation)
			throws IOException, MalformedURLException {
		final String jarext = ".jar";
		if (downloadLocation.indexOf(jarext) == -1) {
			url = url + COOKIE_TEST_1;
			HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
			con.setInstanceFollowRedirects(false);
			con.connect();
			String actualURL = con.getURL().toString();
			int retryCount = 0;
			String headerLocation;
			while (con.getResponseCode() != 200 || actualURL.indexOf(jarext) == -1) {
				headerLocation = con.getHeaderField("Location");
				if (headerLocation != null) {
					actualURL = headerLocation;
				} else {
					actualURL = con.getURL().toString();
				}
				if (retryCount > RETRY_COUNTER) {
					break;
				}
				con = (HttpURLConnection) (new URL(url).openConnection());
				retryCount++;
			}

			if (actualURL.substring(actualURL.lastIndexOf('/') + 1).indexOf(jarext) != -1)
				downloadLocation = actualURL.substring(actualURL.lastIndexOf('/') + 1);
			else
				downloadLocation = projectName + jarext;
		}

		return downloadLocation.replace("%20", " ");
	}

}

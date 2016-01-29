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
	
	static Logger logger = LogManager.getRootLogger();

	public static void main(String[] args) {
		if(args.length < 1){
			logger.error("Incorrect arguments");
			return;
		}
		String manifestFile = args[0];
		JSONParser parser = new JSONParser();
		try {
			Long projectID;
			Long fileID;
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(manifestFile));
			JSONArray fileList = (JSONArray) jsonObject.get("files");
			logger.info("Starting download of " + fileList.size() + " mods");
			Iterator iterator = fileList.iterator();
			while (iterator.hasNext()) {
				JSONObject j = ((JSONObject) iterator.next());
				projectID = (Long) j.get("projectID");
				fileID = (Long) j.get("fileID");
				String url = "http://minecraft.curseforge.com/projects/" + projectID + "?cookieTest=1";
				logger.info(url);
				HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
				con.setInstanceFollowRedirects(false);
				con.connect();
				String location = con.getHeaderField("Location");
				String projectName = location.split("/")[2];
				String actualFile = downloadJsonFiles(projectID, projectName);
				JSONObject curseWidget = (JSONObject) parser.parse(new FileReader("cache"+File.separator+actualFile));
				JSONObject fileList1 = (JSONObject) curseWidget.get("files");
				JSONObject fileData = (JSONObject) fileList1.get(fileID.toString());
				logger.info("Getting file ID: " + fileID);
				if (fileData != null) {
					String fileName = (String) fileData.get("name");
					downloadFile(createCurseDownloadUrl(projectName, fileID), fileName, "mods", projectName);
				} else {
					logger.warn("Could not find file in json, attempting to download manually");
					downloadFile(createCurseDownloadUrl(projectName, fileID), null, "mods", projectName);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		logger.info("Finished downloading mods");
	}

	private static String downloadJsonFiles(Long projectID, String projectName) {
		String fileName1 = projectName + ".json";
		String fileName2 = projectID + "-" + projectName + ".json";
		downloadFile(createWidgetUrl(projectName), fileName1, "cache");
		downloadFile(createWidgetUrl(projectID + "-" + projectName), fileName2, "cache");
		File f1 = new File("cache"+File.separator+fileName1);
		File f2 = new File("cache"+File.separator+fileName2);
		String actualFile;
		if (f1.exists() && !f1.isDirectory())
			actualFile = fileName1;
		else
			actualFile = fileName2;
		return actualFile;
	}

	private static void downloadFile(String createWidgetUrl, String fileName2, String folder) {
		downloadFile(createWidgetUrl, fileName2, folder, null);
	}

	private static String createWidgetUrl(String projectName) {
		return "http://widget.mcf.li/mc-mods/minecraft/" + projectName + ".json";
	}

	private static String createCurseDownloadUrl(String projectName, Long fileID) {
		return "http://minecraft.curseforge.com/projects/" + projectName + "/files/" + fileID + "/download";
	}

	private static void downloadFile(String url, String fileName, String folder, String projectName) {
		final String jarext = ".jar";
		final String jsonext = ".json";
		createFolder(folder);
		if (fileName == null) {
			fileName = projectName + jarext;
		}
		logger.info("Downloading " + url + " to file " + fileName);
		String downloadLocation = fileName;
		try {
			URL fileThing = new URL(url);

			downloadLocation = getDownloadLocation(url, projectName, jarext, jsonext, downloadLocation);
			ReadableByteChannel rbc = Channels.newChannel(fileThing.openStream());
			FileOutputStream fos;
			if (folder != null) {
				fos = new FileOutputStream(new File(folder + File.separator + downloadLocation));
			} else {
				fos = new FileOutputStream(new File(downloadLocation));
			}
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			logger.error("Could not find: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getDownloadLocation(String url, String projectName, final String jarext, final String jsonext,
			String downloadLocation) throws IOException, MalformedURLException {
		if (downloadLocation.indexOf(jarext) == -1 && downloadLocation.indexOf(jsonext) == -1) {
			HttpURLConnection con = (HttpURLConnection) (new URL(url + "?cookieTest=1").openConnection());
			con.setInstanceFollowRedirects(false);
			con.connect();

			String actualURL = con.getURL().toString();
			if (actualURL.substring(actualURL.lastIndexOf('/') + 1).indexOf(jarext) != -1)
				downloadLocation = actualURL.substring(actualURL.lastIndexOf('/') + 1);
			else
				downloadLocation = projectName + jarext;
		}
		return downloadLocation;
	}

	private static void createFolder(String folder) {
		if (folder != null) {
			File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

}

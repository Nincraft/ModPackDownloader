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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ModPackDownloader {

	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		try {
			Long projectID;
			Long fileID;
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("src/main/resources/manifest.json"));
			JSONArray fileList = (JSONArray) jsonObject.get("files");
			Iterator iterator = fileList.iterator();
			while (iterator.hasNext()) {
				JSONObject j = ((JSONObject) iterator.next());
				projectID = (Long) j.get("projectID");
				fileID = (Long) j.get("fileID");
				String url = "http://minecraft.curseforge.com/projects/" + projectID + "?cookieTest=1";
				System.out.println(url);
				HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
				con.setInstanceFollowRedirects(false);
				con.connect();
				int responseCode = con.getResponseCode();
				System.out.println(responseCode);
				String location = con.getHeaderField("Location");
				String projectName = location.split("/")[2];
				String fileName1 = projectName + ".json";
				String fileName2 = projectID + "-" + projectName + ".json";
				downloadFile(createWidgetUrl(projectName), fileName1);
				downloadFile(createWidgetUrl(projectID + "-" + projectName), fileName2);
				File f1 = new File(fileName1);
				File f2 = new File(fileName2);
				String actualFile;
				if (f1.exists() && !f1.isDirectory())
					actualFile = fileName1;
				else
					actualFile = fileName2;
				JSONObject curseWidget = (JSONObject) parser.parse(new FileReader(actualFile));
				JSONObject fileList1 = (JSONObject) curseWidget.get("files");
				JSONObject fileData = (JSONObject) fileList1.get(fileID.toString());
				System.out.println("Getting file ID: " + fileID);
				if (fileData != null) {
					String fileName = (String) fileData.get("name");
					downloadFile(createCurseDownloadUrl(projectName, fileID), fileName, null, projectName);
				}
				else{
					System.err.println("Could not find file in json, attempting to download manually");
					downloadFile(createCurseDownloadUrl(projectName, fileID), null, null, projectName);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}

	private static void downloadFile(String createWidgetUrl, String fileName2) {
		downloadFile(createWidgetUrl, fileName2, null, null);
	}

	private static String createWidgetUrl(String projectName) {
		return "http://widget.mcf.li/mc-mods/minecraft/" + projectName + ".json";
	}

	private static String createCurseDownloadUrl(String projectName, Long fileID) {
		return "http://minecraft.curseforge.com/projects/" + projectName + "/files/" + fileID + "/download";
	}

	private static void downloadFile(String url, String fileName, String folder, String projectName) {
		if(fileName == null){
			fileName = projectName+".jar";
		}
		System.out.println("Downloading " + url + " to file " + fileName);
		String downloadLocation;
		if (folder != null) {
			downloadLocation = folder + "/" + fileName;
		} else {
			downloadLocation = fileName;
		}
		try {
			URL fileThing = new URL(url);
			
			if (downloadLocation.indexOf(".jar") == -1 && downloadLocation.indexOf(".json") == -1) {
				HttpURLConnection con = (HttpURLConnection) (new URL(url + "?cookieTest=1").openConnection());
				con.setInstanceFollowRedirects(false);
				con.connect();

				String actualURL = con.getURL().toString();
				if (actualURL.substring(actualURL.lastIndexOf('/') + 1).indexOf(".jar") != -1)
					downloadLocation = actualURL.substring(actualURL.lastIndexOf('/') + 1);
				else
					downloadLocation = projectName + ".jar";
			}
			ReadableByteChannel rbc = Channels.newChannel(fileThing.openStream());
			FileOutputStream fos = new FileOutputStream(downloadLocation);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("Could not find: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

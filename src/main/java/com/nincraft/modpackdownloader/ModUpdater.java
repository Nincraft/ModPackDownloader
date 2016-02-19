package com.nincraft.modpackdownloader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nincraft.modpackdownloader.util.Reference;

public class ModUpdater {

	static Logger logger = LogManager.getRootLogger();

	private static final String[] formats = { "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss'Z'",
			"yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
			"yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", "MM/dd/yyyy'T'HH:mm:ss.SSSZ",
			"MM/dd/yyyy'T'HH:mm:ss.SSS", "MM/dd/yyyy'T'HH:mm:ssZ", "MM/dd/yyyy'T'HH:mm:ss", "yyyy:MM:dd HH:mm:ss", };

	public static void updateCurseMods(String manifestFile, String mcVersion, String releaseType) {
		try {
			Long projectID;
			Long fileID;
			JSONParser parser = new JSONParser();
			JSONObject jsons = (JSONObject) parser.parse(new FileReader(manifestFile));
			JSONArray fileList = (JSONArray) jsons.get("curseFiles");
			if (fileList == null) {
				fileList = (JSONArray) jsons.get("files");
			}
			if (fileList != null) {
				Iterator iterator = fileList.iterator();
				logger.info("Checking for updates from " + fileList.size() + " mods");
				while (iterator.hasNext()) {
					JSONObject modJson = (JSONObject) iterator.next();
					projectID = (Long) modJson.get("projectID");
					fileID = (Long) modJson.get("fileID");
					String url = Reference.CURSEFORGE_BASE_URL + projectID + Reference.COOKIE_TEST_1;
					HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
					con.setInstanceFollowRedirects(false);
					con.connect();
					String location = con.getHeaderField("Location");
					String projectName = location.split("/")[2];
					JSONParser projectParser = new JSONParser();
					JSONObject projectJson = getCurseProjectJson(projectID, projectName, projectParser);
					JSONObject fileListJson = (JSONObject) projectJson.get("files");
					Date lastDate = null;
					Long mostRecent = fileID;
					String mostRecentFile = null;
					String currentFile = null;
					for (Object thing : fileListJson.keySet()) {
						JSONObject file = (JSONObject) fileListJson.get(thing);
						Date date = parseDate((String) file.get("created_at"));
						if (lastDate == null) {
							lastDate = date;
						}
						if (lastDate.before(date) && equalOrLessThan((String) file.get("type"), releaseType)
								&& file.get("version").equals(mcVersion)) {
							mostRecent = (Long) file.get("id");
							mostRecentFile = (String) file.get("name");
							lastDate = date;
						}
						if (fileID.equals((Long) file.get("id"))) {
							currentFile = (String) file.get("name");
						}
					}
					if (!mostRecent.equals(fileID)) {
						logger.info("Update found for " + projectName + ". Most recent version is " + mostRecentFile
								+ ". Old version was " + currentFile);

						modJson.remove("fileID");
						modJson.put("fileID", mostRecent);
					}
					if (!modJson.containsKey("name")) {
						modJson.put("name", projectName);
					}
				}
			}
			FileWriter file = new FileWriter(manifestFile);
			try {
				file.write(jsons.toJSONString());
			} finally {
				file.flush();
				file.close();
			}

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
	}

	private static boolean equalOrLessThan(String modRelease, String releaseType) {
		if (releaseType.equals(modRelease) || "beta".equals(releaseType) && "release".equals(modRelease)) {
			return true;
		}
		return false;
	}

	private static JSONObject getCurseProjectJson(Long projectID, String projectName, JSONParser projectParser)
			throws ParseException, IOException {
		try {
			return (JSONObject) projectParser.parse(new BufferedReader(new InputStreamReader(
					new URL("http://widget.mcf.li/mc-mods/minecraft/" + projectName + ".json").openStream())));
		} catch (FileNotFoundException e) {
			return (JSONObject) projectParser.parse(new BufferedReader(new InputStreamReader(
					new URL("http://widget.mcf.li/mc-mods/minecraft/" + projectID + "-" + projectName + ".json")
							.openStream())));
		}
	}

	private static Date parseDate(String date) {
		Date d = null;
		if (date != null) {
			for (String parse : formats) {
				SimpleDateFormat sdf = new SimpleDateFormat(parse);
				try {
					d = sdf.parse(date);
				} catch (java.text.ParseException e) {

				}
			}
		}
		return d;
	}

}

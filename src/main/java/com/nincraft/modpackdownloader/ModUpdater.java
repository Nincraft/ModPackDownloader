package com.nincraft.modpackdownloader;

import static com.nincraft.modpackdownloader.util.Reference.COOKIE_TEST_1;
import static com.nincraft.modpackdownloader.util.Reference.CURSEFORGE_BASE_URL;
import static com.nincraft.modpackdownloader.util.Reference.DATE_FORMATS;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nincraft.modpackdownloader.util.Reference;

import lombok.val;

public class ModUpdater {
	static Logger logger = LogManager.getRootLogger();

	public static void updateCurseMods(final String manifestFile, final String mcVersion, final String releaseType) {
		try {
			val jsons = (JSONObject) new JSONParser().parse(new FileReader(manifestFile));
			val fileList = (JSONArray) (jsons.containsKey("curseFiles") ? jsons.get("curseFiles") : jsons.get("files"));

			if (fileList != null) {
				logger.info("Checking for updates from " + fileList.size() + " mods");
				for (val file : fileList) {
					val modJson = (JSONObject) file;
					val projectID = (Long) modJson.get("projectID");
					val fileID = (Long) modJson.get("fileID");
					val url = CURSEFORGE_BASE_URL + projectID + COOKIE_TEST_1;

					final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
					con.setInstanceFollowRedirects(false);
					con.connect();

					val location = con.getHeaderField("Location");
					val projectName = location.split("/")[2];
					val projectParser = new JSONParser();
					val fileListJson = (JSONObject) getCurseProjectJson(projectID, projectName, projectParser)
							.get("files");

					if (fileListJson == null) {
						logger.error(String.format("No file list found for {}.", projectName));
						return;
					}

					Date lastDate = null;
					Long mostRecent = fileID;
					String mostRecentFile = null;
					String currentFile = null;

					for (val thing : fileListJson.values()) {
						val mod = (JSONObject) fileListJson.get(thing);
						val date = parseDate((String) mod.get("created_at"));

						if (lastDate == null) {
							lastDate = date;
						}

						if (lastDate.before(date) && equalOrLessThan((String) mod.get("type"), releaseType)
								&& mod.get("version").equals(mcVersion)) {
							mostRecent = (Long) mod.get("id");
							mostRecentFile = (String) mod.get("name");
							lastDate = date;
						}

						if (fileID.equals(mod.get("id"))) {
							currentFile = (String) mod.get("name");
						}
					}

					if (!mostRecent.equals(fileID)) {
						logger.info(
								String.format("Update found for {}.  Most recent version is {}.  Old version was {}.",
										projectName, mostRecentFile, currentFile));

						modJson.remove("fileID");
						modJson.put("fileID", mostRecent);
					}

					if (!modJson.containsKey("name")) {
						modJson.put("name", projectName);
					}
				}
			}

			val file = new FileWriter(manifestFile);

			try {
				file.write(jsons.toJSONString());
			} finally {
				file.flush();
				file.close();
			}
		} catch (final IOException e) {
			logger.error(e.getMessage());
		} catch (final ParseException e) {
			logger.error(e.getMessage());
		}
	}

	private static boolean equalOrLessThan(final String modRelease, final String releaseType) {
		return releaseType.equals(modRelease) || "beta".equals(releaseType) && "release".equals(modRelease);
	}

	private static JSONObject getCurseProjectJson(final Long projectID, final String projectName,
			final JSONParser projectParser) throws ParseException, IOException {
		try {
			return (JSONObject) projectParser.parse(new BufferedReader(new InputStreamReader(
					new URL(String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, projectName)).openStream())));
		} catch (final FileNotFoundException e) {
			return (JSONObject) projectParser.parse(new BufferedReader(new InputStreamReader(
					new URL(String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, projectID + "-" + projectName))
							.openStream())));
		}
	}

	private static Date parseDate(final String date) {
		for (val parse : DATE_FORMATS) {
			try {
				return new SimpleDateFormat(parse).parse(date);
			} catch (final java.text.ParseException e) {
			}
		}
		return null;
	}

}

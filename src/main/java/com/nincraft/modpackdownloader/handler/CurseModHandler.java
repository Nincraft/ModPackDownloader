package com.nincraft.modpackdownloader.handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CurseModHandler extends ModHandler {

	@Override
	public void downloadMod(final Mod mod) {
		downloadCurseMod((CurseFile) mod);
	}

	@Override
	public void updateMod(final Mod mod) {
		updateCurseMod((CurseFile) mod);
	}

	private static void downloadCurseMod(final CurseFile mod) {
		val modName = mod.getName();

		try {
			val fileName = !Strings.isNullOrEmpty(mod.getRename()) ? mod.getRename()
					: getCurseForgeDownloadLocation(mod.getDownloadUrl(), modName, modName);
			mod.setFileName(fileName);

			downloadFile(mod, false);
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
	}

	private static String getCurseForgeDownloadLocation(final String url, final String projectName,
			final String downloadLocation) throws IOException, MalformedURLException {
		String encodedDownloadLocation = URLHelper.encodeSpaces(downloadLocation);

		if (encodedDownloadLocation.indexOf(Reference.JAR_FILE_EXT) == -1) {
			val newUrl = url + Reference.COOKIE_TEST_1;

			HttpURLConnection conn = (HttpURLConnection) new URL(newUrl).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			String actualURL = conn.getURL().toString();
			int retryCount = 0;

			while (conn.getResponseCode() != 200 || actualURL.indexOf(Reference.JAR_FILE_EXT) == -1) {
				val headerLocation = conn.getHeaderField("Location");
				if (headerLocation != null) {
					actualURL = headerLocation;
				} else {
					actualURL = conn.getURL().toString();
				}

				if (retryCount > Reference.RETRY_COUNTER) {
					break;
				}

				conn = (HttpURLConnection) new URL(newUrl).openConnection();
				retryCount++;
			}

			if (actualURL.substring(actualURL.lastIndexOf(Reference.URL_DELIMITER) + 1)
					.indexOf(Reference.JAR_FILE_EXT) != -1) {
				encodedDownloadLocation = actualURL.substring(actualURL.lastIndexOf(Reference.URL_DELIMITER) + 1);
			} else {
				encodedDownloadLocation = projectName + Reference.JAR_FILE_EXT;
			}
		}

		return URLHelper.decodeSpaces(encodedDownloadLocation);
	}

	private static void updateCurseMod(final CurseFile mod) {
		JSONObject fileListJson = null;
		try {
			val conn = (HttpURLConnection) new URL(mod.getProjectUrl()).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			val location = conn.getHeaderField("Location");
			mod.setProjectName(location.split("/")[2]);
			fileListJson = (JSONObject) getCurseProjectJson(mod.getProjectID(), mod.getProjectName(), new JSONParser())
					.get("files");

			if (fileListJson == null) {
				log.error(String.format("No file list found for %s, and will be skipped.", mod.getProjectName()));
				return;
			}
		} catch (IOException | ParseException e) {
			log.error(e.getMessage());
			return;
		}

		val newMod = getLatestVersion(Reference.mcVersion, Reference.releaseType, mod, fileListJson);
		if (mod.getFileID().compareTo(newMod.getFileID()) < 0) {
			log.info(String.format("Update found for %s.  Most recent version is %s.  Old version was %s.",
					mod.getProjectName(), newMod.getVersion(), mod.getVersion()));
			mod.setFileID(newMod.getFileID());
			mod.setVersion(newMod.getVersion());
		}

		if (Strings.isNullOrEmpty(mod.getName())) {
			mod.setName(mod.getProjectName());
		}
	}

	private static CurseFile getLatestVersion(final String mcVersion, final String releaseType,
			final CurseFile curseMod, final JSONObject fileListJson) {
		log.trace("Getting most recent available file...");
		CurseFile newMod = null;
		try {
			newMod = (CurseFile) curseMod.clone();
		} catch (CloneNotSupportedException e) {
			log.warn("Couldn't clone existing mod reference, creating new one instead.");
			newMod = new CurseFile();
		}

		Date latestDate = null;
		for (val newFileJson : fileListJson.values()) {
			val newModJson = (JSONObject) newFileJson;
			val date = parseDate((String) newModJson.get("created_at"));

			if(latestDate == null){
				latestDate = date;
			}
			if (!latestDate.after(date) && equalOrLessThan((String) newModJson.get("type"), releaseType)
					&& newModJson.get("version").equals(mcVersion)) {
				newMod.setFileID(((Long) newModJson.get("id")).intValue());
				newMod.setVersion((String) newModJson.get("name"));
				latestDate = date;
			}

			if (curseMod.getFileID().equals(newMod.getFileID())) {
				log.debug("Ensuring the current version is set on the mod.");
				curseMod.setVersion(newMod.getVersion());
			}
		}
		log.trace("Finished getting most recent available file.");
		return newMod;
	}

	private static boolean equalOrLessThan(final String modRelease, final String releaseType) {
		return releaseType.equals(modRelease) || "beta".equals(releaseType) && "release".equals(modRelease);
	}

	private static JSONObject getCurseProjectJson(final Integer integer, final String projectName,
			final JSONParser projectParser) throws ParseException, IOException {
		log.trace("Getting CurseForge Widget JSON...");
		try {
			String urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, projectName);
			log.debug(urlStr);
			return (JSONObject) projectParser
					.parse(new BufferedReader(new InputStreamReader(new URL(urlStr).openStream())));
		} catch (final FileNotFoundException e) {
			String urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, integer + "-" + projectName);
			log.debug(urlStr);
			return (JSONObject) projectParser
					.parse(new BufferedReader(new InputStreamReader(new URL(urlStr).openStream())));
		} finally {
			log.trace("Finished Getting CurseForge Widget JSON.");
		}
	}

	private static Date parseDate(final String date) {
		for (val parse : Reference.DATE_FORMATS) {
			try {
				return new SimpleDateFormat(parse).parse(date);
			} catch (final java.text.ParseException e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}
}

package com.nincraft.modpackdownloader.handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
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
		if(BooleanUtils.isTrue(mod.getSkipDownload())){
			log.info(String.format("Skipped downloading %s", mod.getName()));
			return;
		}

		val modName = mod.getName();

		try {
			val fileName = !Strings.isNullOrEmpty(mod.getRename()) ? mod.getRename()
					: getCurseForgeDownloadLocation(mod.getDownloadUrl(), modName, modName);
			mod.setFileName(fileName);

			downloadFile(mod);
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
	}

	private static String getCurseForgeDownloadLocation(final String url, final String projectName,
			final String downloadLocation) throws IOException, MalformedURLException {
		String encodedDownloadLocation = URLHelper.encodeSpaces(downloadLocation);

		if (!encodedDownloadLocation.contains(Reference.JAR_FILE_EXT)) {
			val newUrl = url + Reference.COOKIE_TEST_1;

			HttpURLConnection conn = (HttpURLConnection) new URL(newUrl).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			String actualURL = conn.getURL().toString();
			int retryCount = 0;

			while (conn.getResponseCode() != 200 || !actualURL.contains(Reference.JAR_FILE_EXT)) {
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

			if (actualURL.substring(actualURL.lastIndexOf(Reference.URL_DELIMITER) + 1).contains(Reference.JAR_FILE_EXT)) {
				encodedDownloadLocation = actualURL.substring(actualURL.lastIndexOf(Reference.URL_DELIMITER) + 1);
			} else {
				encodedDownloadLocation = projectName + Reference.JAR_FILE_EXT;
			}
		}

		return URLHelper.decodeSpaces(encodedDownloadLocation);
	}

	private static void updateCurseMod(final CurseFile mod) {
		if (BooleanUtils.isTrue(mod.getSkipUpdate())) {
			log.info("Skipped updating " + mod.getName());
			return;
		}
		JSONObject fileListJson = null;
		try {
			val conn = (HttpURLConnection) new URL(mod.getProjectUrl()).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			fileListJson = (JSONObject) getCurseProjectJson(mod.getProjectID(), mod.getProjectName(), new JSONParser())
					.get("files");

			if (fileListJson == null) {
				log.error(String.format("No file list found for %s, and will be skipped.", mod.getName()));
				return;
			}
		} catch (IOException | ParseException e) {
			log.error(e.getMessage());
			return;
		}

		val newMod = getLatestVersion(Reference.mcVersion,
				mod.getReleaseType() != null ? mod.getReleaseType() : Reference.releaseType, mod, fileListJson);
		if (mod.getFileID().compareTo(newMod.getFileID()) < 0) {
			log.info(String.format("Update found for %s.  Most recent version is %s.", mod.getName(),
					newMod.getVersion()));
			mod.setFileID(newMod.getFileID());
			mod.setVersion(newMod.getVersion());
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

		List<JSONObject> fileList = new ArrayList<JSONObject>(fileListJson.values());
		List<Long> fileIds = new ArrayList<Long>();
		for (JSONObject file : fileList) {
			if (equalOrLessThan((String) file.get("type"), releaseType) && file.get("version").equals(mcVersion)) {
				fileIds.add((Long) file.get("id"));
			}
		}
		Collections.sort(fileIds);
		Collections.reverse(fileIds);
		if (!fileIds.isEmpty() && fileIds.get(0).intValue() != curseMod.getFileID()) {
			newMod.setFileID(fileIds.get(0).intValue());
			newMod.setVersion((String) ((JSONObject) fileListJson.get(newMod.getFileID().toString())).get("name"));
		}
		if (!"alpha".equals(releaseType) && fileIds.isEmpty()) {
			log.info(String.format("No files found for this Minecraft version, disabling download of %s", curseMod.getName()));
			curseMod.setSkipDownload(true);
		}
		if (BooleanUtils.isTrue(curseMod.getSkipUpdate()) && !fileIds.isEmpty()) {
			log.info(String.format("Found files for this Minecraft version, enabling download of %s", curseMod.getName()));
			curseMod.setSkipDownload(null);
		}

		log.trace("Finished getting most recent available file.");
		return newMod;
	}

	private static boolean equalOrLessThan(final String modRelease, final String releaseType) {
		return "alpha".equals(releaseType) || releaseType.equals(modRelease)
				|| "beta".equals(releaseType) && "release".equals(modRelease);
	}

	private static JSONObject getCurseProjectJson(final Integer projectId, final String projectName,
			final JSONParser projectParser) throws ParseException, IOException {
		log.trace("Getting CurseForge Widget JSON...");
		try {
			String urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, projectName);
			log.debug(urlStr);
			return (JSONObject) projectParser
					.parse(new BufferedReader(new InputStreamReader(new URL(urlStr).openStream())));
		} catch (final FileNotFoundException e) {
			String urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, projectId + "-" + projectName);
			log.debug(urlStr);
			return (JSONObject) projectParser
					.parse(new BufferedReader(new InputStreamReader(new URL(urlStr).openStream())));
		} finally {
			log.trace("Finished Getting CurseForge Widget JSON.");
		}
	}
}

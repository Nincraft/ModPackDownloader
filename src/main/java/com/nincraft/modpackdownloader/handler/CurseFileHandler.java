package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public class CurseFileHandler extends ModHandler {

	private static void downloadCurseMod(CurseFile curseFile) {
		try {
			curseFile = getCurseForgeDownloadLocation(curseFile);
			DownloadHelper.downloadFile(curseFile);
		} catch (IOException e) {
			log.error(e);
		}
	}

	private static CurseFile getCurseForgeDownloadLocation(final CurseFile curseFile) throws IOException {
		String url = curseFile.getCurseForgeDownloadUrl();
		String projectName = curseFile.getName();
		String encodedDownloadLocation = URLHelper.encodeSpaces(projectName);

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
			curseFile.setDownloadUrl(actualURL.replace("http:", "https:"));
		}
		curseFile.setFileName(URLHelper.decodeSpaces(encodedDownloadLocation));

		return curseFile;
	}

	public static void updateCurseFile(final CurseFile curseFile) {
		if (BooleanUtils.isTrue(curseFile.getSkipUpdate())) {
			log.info("Skipped updating " + curseFile.getName());
			return;
		}
		JSONObject fileListJson = null;
		try {
			val conn = (HttpURLConnection) new URL(curseFile.getProjectUrl()).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			fileListJson = (JSONObject) getCurseProjectJson(curseFile, new JSONParser())
					.get("files");

			if (fileListJson == null) {
				log.error(String.format("No file list found for %s, and will be skipped.", curseFile.getName()));
				return;
			}
		} catch (IOException | ParseException e) {
			log.error(e);
			return;
		}

		val newMod = getLatestVersion(curseFile.getReleaseType() != null ? curseFile.getReleaseType() : Reference.releaseType, curseFile, fileListJson);
		if (curseFile.getFileID().compareTo(newMod.getFileID()) < 0) {
			log.info(String.format("Update found for %s.  Most recent version is %s.", curseFile.getName(),
					newMod.getVersion()));
			updateCurseFile(curseFile, newMod);
		}
	}

	private static void updateCurseFile(CurseFile curseFile, CurseFile newMod) {
		curseFile.setFileID(newMod.getFileID());
		curseFile.setVersion(newMod.getVersion());
		if (curseFile.isModpack()) {
			curseFile.setFileName(newMod.getVersion());
		}
	}

	private static CurseFile getLatestVersion(String releaseType,
											  CurseFile curseFile, final JSONObject fileListJson) {
		log.trace("Getting most recent available file...");
		if (Strings.isNullOrEmpty(releaseType)) {
			releaseType = "release";
		}
		CurseFile newMod = null;
		try {
			newMod = (CurseFile) curseFile.clone();
		} catch (CloneNotSupportedException e) {
			log.warn("Couldn't clone existing mod reference, creating new one instead.");
			newMod = new CurseFile();
		}

		curseFile = checkFileId(curseFile);

		List<JSONObject> fileList = new ArrayList<JSONObject>(fileListJson.values());
		List<Long> fileIds = new ArrayList<Long>();
		for (JSONObject file : fileList) {
			if (equalOrLessThan((String) file.get("type"), releaseType) && isMcVersion((String) file.get("version"))) {
				fileIds.add((Long) file.get("id"));
			}
		}
		Collections.sort(fileIds);
		Collections.reverse(fileIds);
		if (!fileIds.isEmpty() && fileIds.get(0).intValue() != curseFile.getFileID()) {
			newMod.setFileID(fileIds.get(0).intValue());
			newMod.setVersion((String) ((JSONObject) fileListJson.get(newMod.getFileID().toString())).get("name"));
		}
		if (!"alpha".equals(releaseType) && fileIds.isEmpty()) {
			log.info(String.format("No files found for this Minecraft version, disabling download of %s", curseFile.getName()));
			curseFile.setSkipDownload(true);
		}
		if (BooleanUtils.isTrue(curseFile.getSkipUpdate()) && !fileIds.isEmpty()) {
			log.info(String.format("Found files for this Minecraft version, enabling download of %s", curseFile.getName()));
			curseFile.setSkipDownload(null);
		}

		log.trace("Finished getting most recent available file.");
		return newMod;
	}

	private static boolean isMcVersion(String version) {
		if ("*".equals(Reference.mcVersion)) {
			return true;
		} else {
			return Reference.mcVersion.equals(version);
		}
	}

	private static CurseFile checkFileId(CurseFile curseFile) {
		if (curseFile.getFileID() == null) {
			curseFile.setFileID(0);
		}
		return curseFile;
	}

	private static boolean equalOrLessThan(final String modRelease, final String releaseType) {
		return "alpha".equals(releaseType) || releaseType.equals(modRelease)
				|| "beta".equals(releaseType) && "release".equals(modRelease);
	}

	private static JSONObject getCurseProjectJson(final CurseFile curseFile,
												  final JSONParser projectParser) throws ParseException, IOException {
		log.trace("Getting CurseForge Widget JSON...");
		Integer projectId = curseFile.getProjectID();
		String projectName = curseFile.getProjectName();
		String modOrModPack = curseFile.isModpack() ? Reference.CURSEFORGE_WIDGET_JSON_MODPACK : Reference.CURSEFORGE_WIDGET_JSON_MOD;
		String urlStr;
		try {
			urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, modOrModPack, projectName);
			log.debug(urlStr);
			return (JSONObject) projectParser
					.parse(new BufferedReader(new InputStreamReader(new URL(urlStr).openStream())));
		} catch (final FileNotFoundException e) {
			urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, modOrModPack, projectId + "-" + projectName);
			log.debug(urlStr, e);
			return (JSONObject) projectParser
					.parse(new BufferedReader(new InputStreamReader(new URL(urlStr).openStream())));
		} finally {
			log.trace("Finished Getting CurseForge Widget JSON.");
		}
	}

	@Override
	public void downloadMod(final Mod mod) {
		downloadCurseMod((CurseFile) mod);
	}

	@Override
	public void updateMod(final Mod mod) {
		updateCurseFile((CurseFile) mod);
	}
}

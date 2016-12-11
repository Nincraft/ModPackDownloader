package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public class CurseFileHandler extends ModHandler {

	private static void downloadCurseMod(CurseFile curseFile) {
		try {
			DownloadHelper.getInstance().downloadFile(getCurseForgeDownloadLocation(curseFile));
		} catch (IOException e) {
			log.error(e);
		}
	}

	private static CurseFile getCurseForgeDownloadLocation(final CurseFile curseFile) throws IOException {
		val url = curseFile.getCurseForgeDownloadUrl();
		val projectName = curseFile.getName();
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

			int lastIndexUrl = actualURL.lastIndexOf(Reference.URL_DELIMITER) + 1;
			if (actualURL.substring(lastIndexUrl).contains(Reference.JAR_FILE_EXT)) {
				encodedDownloadLocation = actualURL.substring(lastIndexUrl);
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

			fileListJson = (JSONObject) getCurseProjectJson(curseFile).get("files");

			if (fileListJson == null) {
				log.error(String.format("No file list found for %s, and will be skipped.", curseFile.getName()));
				return;
			}
		} catch (IOException | ParseException e) {
			log.error(e);
			return;
		}

		val newMod = getLatestVersion(curseFile.getReleaseType() != null ? curseFile.getReleaseType() : Arguments.releaseType, curseFile, fileListJson, null);
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
											  CurseFile curseFile, final JSONObject fileListJson, String mcVersion) {
		log.trace("Getting most recent available file...");
		boolean backup = true;
		if (Strings.isNullOrEmpty(mcVersion)) {
			mcVersion = Arguments.mcVersion;
			backup = false;
		}
		releaseType = defaultReleaseType(releaseType);
		CurseFile newMod = new CurseFile(curseFile);
		curseFile = checkFileId(curseFile);

		List<JSONObject> fileList = new ArrayList<>(fileListJson.values());
		List<Long> fileIds = new ArrayList<>();
		checkFileIds(releaseType, mcVersion, fileList, fileIds);
		Collections.sort(fileIds);
		Collections.reverse(fileIds);
		setUpdatedFileId(curseFile, fileListJson, newMod, fileIds);

		if (!"alpha".equals(releaseType) && fileIds.isEmpty()) {
			if (CollectionUtils.isEmpty(Arguments.backupVersions)) {
				log.info(String.format("No files found for Minecraft %s, disabling download of %s", mcVersion, curseFile.getName()));
				curseFile.setSkipDownload(true);
			} else if (!backup) {
				newMod = checkBackupVersions(releaseType, curseFile, fileListJson, mcVersion, newMod);
			} else if (fileIds.isEmpty()) {
				curseFile.setSkipDownload(true);
				newMod.setSkipDownload(true);
			}
		}
		if (BooleanUtils.isTrue(curseFile.getSkipDownload()) && !fileIds.isEmpty()) {
			log.info(String.format("Found files for Minecraft %s, enabling download of %s", mcVersion, curseFile.getName()));
			newMod.setSkipDownload(null);
		}

		log.trace("Finished getting most recent available file.");
		return newMod;
	}

	private static void setUpdatedFileId(CurseFile curseFile, JSONObject fileListJson, CurseFile newMod, List<Long> fileIds) {
		if (!fileIds.isEmpty() && fileIds.get(0).intValue() != curseFile.getFileID()) {
			newMod.setFileID(fileIds.get(0).intValue());
			newMod.setVersion((String) ((JSONObject) fileListJson.get(newMod.getFileID().toString())).get("name"));
		}
	}

	private static void checkFileIds(String releaseType, String mcVersion, List<JSONObject> fileList, List<Long> fileIds) {
		for (JSONObject file : fileList) {
			if (equalOrLessThan((String) file.get("type"), releaseType) && isMcVersion((String) file.get("version"), mcVersion)) {
				fileIds.add((Long) file.get("id"));
			}
		}
	}

	private static String defaultReleaseType(String releaseType) {
		if (Strings.isNullOrEmpty(releaseType)) {
			releaseType = "release";
		}
		return releaseType;
	}

	private static CurseFile checkBackupVersions(String releaseType, CurseFile curseFile, JSONObject fileListJson, String mcVersion, CurseFile newMod) {
		for (String backupVersion : Arguments.backupVersions) {
			log.info(String.format("No files found for Minecraft %s, checking backup version %s", mcVersion, backupVersion));
			newMod = getLatestVersion(releaseType, curseFile, fileListJson, backupVersion);
			if (BooleanUtils.isFalse(newMod.getSkipDownload())) {
				curseFile.setSkipDownload(null);
				log.info(String.format("Found update for %s in Minecraft %s", curseFile.getName(), backupVersion));
				break;
			}
		}
		return newMod;
	}

	private static boolean isMcVersion(String modVersion, String argVersion) {
		return "*".equals(argVersion) || argVersion.equals(modVersion);
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

	private static JSONObject getCurseProjectJson(final CurseFile curseFile) throws ParseException, IOException {
		log.trace("Getting CurseForge Widget JSON...");
		val projectId = curseFile.getProjectID();
		val projectName = curseFile.getProjectName();
		val modOrModPack = curseFile.isModpack() ? Reference.CURSEFORGE_WIDGET_JSON_MODPACK : Reference.CURSEFORGE_WIDGET_JSON_MOD;
		String urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, modOrModPack, projectName);
		log.debug(urlStr);
		try {
			return URLHelper.getJsonFromUrl(urlStr);
		} catch (final FileNotFoundException e) {
			urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, modOrModPack, projectId + "-" + projectName);
			log.debug(urlStr, e);
			return URLHelper.getJsonFromUrl(urlStr);
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

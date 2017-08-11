package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.CurseModpackFile;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.summary.UpdateCheckSummarizer;
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
public class CurseFileHandler implements ModHandler {

	private Reference reference = Reference.getInstance();
	private UpdateCheckSummarizer updateCheckSummarizer = UpdateCheckSummarizer.getInstance();
	private Arguments arguments;
	private DownloadHelper downloadHelper;

	public CurseFileHandler(Arguments arguments, DownloadHelper downloadHelper) {
		this.arguments = arguments;
		this.downloadHelper = downloadHelper;
	}

	private void downloadCurseMod(CurseFile curseFile) {
		try {
			downloadHelper.downloadFile(getCurseForgeDownloadLocation(curseFile));
		} catch (IOException e) {
			log.error(e);
		}
	}

	public CurseFile getCurseForgeDownloadLocation(final CurseFile curseFile) throws IOException {
		val url = curseFile.getCurseForgeDownloadUrl();
		val projectName = curseFile.getName();
		String encodedDownloadLocation = URLHelper.encodeSpaces(projectName);

		if (!encodedDownloadLocation.contains(reference.getJarFileExt()) || !encodedDownloadLocation.contains(reference.getZipFileExt())) {
			val newUrl = url + reference.getCookieTest1();

			HttpURLConnection conn = (HttpURLConnection) new URL(newUrl).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			String actualURL = conn.getURL().toString();
			int retryCount = 0;

			while (conn.getResponseCode() != 200 || !actualURL.contains(curseFile.getFileExtension())) {
				val headerLocation = conn.getHeaderField("Location");
				if (headerLocation != null) {
					actualURL = headerLocation;
				} else {
					actualURL = conn.getURL().toString();
				}

				if (retryCount > reference.getRetryCounter()) {
					break;
				}

				conn = (HttpURLConnection) new URL(newUrl).openConnection();
				retryCount++;
			}

			encodedDownloadLocation = getEncodedDownloadLocation(projectName, actualURL, actualURL.lastIndexOf(reference.getUrlDelimiter()) + 1);
			curseFile.setDownloadUrl(actualURL.replace("http:", "https:"));
		}
		curseFile.setFileName(URLHelper.decodeSpaces(encodedDownloadLocation));

		return curseFile;
	}

	private String getEncodedDownloadLocation(String projectName, String actualURL, int lastIndexUrl) {
		String encodedDownloadLocation;
		if (actualURL.substring(lastIndexUrl).contains(reference.getJarFileExt()) || actualURL.substring(lastIndexUrl).contains(reference.getZipFileExt())) {
			encodedDownloadLocation = actualURL.substring(lastIndexUrl);
		} else {
			encodedDownloadLocation = projectName + reference.getJarFileExt();
		}
		return encodedDownloadLocation;
	}

	public void updateCurseFile(final CurseFile curseFile) {
		if (BooleanUtils.isTrue(curseFile.getSkipUpdate())) {
			log.debug("Skipped updating {}", curseFile.getName());
			return;
		}
		JSONObject fileListJson;
		try {
			val conn = (HttpURLConnection) new URL(curseFile.getProjectUrl()).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			fileListJson = (JSONObject) getCurseProjectJson(curseFile).get("files");

			if (fileListJson == null) {
				log.error("No file list found for {}, and will be skipped.", curseFile.getName());
				return;
			}
		} catch (IOException | ParseException e) {
			log.error(e);
			return;
		}

		val newMod = getLatestVersion(curseFile.getReleaseType() != null ? curseFile.getReleaseType() : arguments.getReleaseType(), curseFile, fileListJson, null);
		if (curseFile.getFileID().compareTo(newMod.getFileID()) < 0) {
			log.debug("Update found for {}.  Most recent version is {}.", curseFile.getName(), newMod.getVersion());
			updateCurseFile(curseFile, newMod);
			updateCheckSummarizer.getModList().add(curseFile);
		}
	}

	private void updateCurseFile(CurseFile curseFile, CurseFile newMod) {
		curseFile.setFileID(newMod.getFileID());
		curseFile.setVersion(newMod.getVersion());
		if (curseFile instanceof CurseModpackFile) {
			curseFile.setFileName(newMod.getVersion());
		}
	}

	private CurseFile getLatestVersion(String releaseType,
									   CurseFile curseFile, final JSONObject fileListJson, String mcVersion) {
		log.trace("Getting most recent available file...");
		boolean backup = true;
		if (Strings.isNullOrEmpty(mcVersion)) {
			mcVersion = arguments.getMcVersion();
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
			if (CollectionUtils.isEmpty(arguments.getBackupVersions())) {
				log.debug("No files found for Minecraft {}, disabling download of {}", mcVersion, curseFile.getName());
				curseFile.setSkipDownload(true);
			} else if (!backup) {
				newMod = checkBackupVersions(releaseType, curseFile, fileListJson, mcVersion, newMod);
			} else if (fileIds.isEmpty()) {
				curseFile.setSkipDownload(true);
				newMod.setSkipDownload(true);
			}
		}
		if (BooleanUtils.isTrue(curseFile.getSkipDownload()) && !fileIds.isEmpty()) {
			log.debug("Found files for Minecraft {}, enabling download of {}", mcVersion, curseFile.getName());
			newMod.setSkipDownload(null);
		}

		log.trace("Finished getting most recent available file.");
		return newMod;
	}

	private void setUpdatedFileId(CurseFile curseFile, JSONObject fileListJson, CurseFile newMod, List<Long> fileIds) {
		if (!fileIds.isEmpty() && fileIds.get(0).intValue() != curseFile.getFileID()) {
			newMod.setFileID(fileIds.get(0).intValue());
			newMod.setVersion((String) ((JSONObject) fileListJson.get(newMod.getFileID().toString())).get("name"));
		}
	}

	private void checkFileIds(String releaseType, String mcVersion, List<JSONObject> fileList, List<Long> fileIds) {
		for (JSONObject file : fileList) {
			if (equalOrLessThan((String) file.get("type"), releaseType) && isMcVersion((String) file.get("version"), mcVersion)) {
				fileIds.add((Long) file.get("id"));
			}
		}
	}

	private String defaultReleaseType(String releaseType) {
		return Strings.isNullOrEmpty(releaseType) ? "release" : releaseType;
	}

	private CurseFile checkBackupVersions(String releaseType, CurseFile curseFile, JSONObject fileListJson, String mcVersion, CurseFile newMod) {
		CurseFile returnMod = newMod;
		for (String backupVersion : arguments.getBackupVersions()) {
			log.debug("No files found for Minecraft {}, checking backup version {}", mcVersion, backupVersion);
			returnMod = getLatestVersion(releaseType, curseFile, fileListJson, backupVersion);
			if (BooleanUtils.isFalse(newMod.getSkipDownload())) {
				curseFile.setSkipDownload(null);
				log.debug("Found update for {} in Minecraft {}", curseFile.getName(), backupVersion);
				break;
			}
		}
		return returnMod;
	}

	private boolean isMcVersion(String modVersion, String argVersion) {
		return "*".equals(argVersion) || argVersion.equals(modVersion);
	}

	private CurseFile checkFileId(CurseFile curseFile) {
		if (curseFile.getFileID() == null) {
			curseFile.setFileID(0);
		}
		return curseFile;
	}

	private boolean equalOrLessThan(final String modRelease, final String releaseType) {
		return "alpha".equals(releaseType) || releaseType.equals(modRelease)
				|| "beta".equals(releaseType) && "release".equals(modRelease);
	}

	private JSONObject getCurseProjectJson(final CurseFile curseFile) throws ParseException, IOException {
		log.trace("Getting CurseForge Widget JSON...");
		val projectId = curseFile.getProjectID();
		val projectName = curseFile.getProjectName();
		val modOrModPack = curseFile.getCurseforgeWidgetJson();
		String urlStr = String.format(reference.getCurseforgeWidgetJsonUrl(), modOrModPack, projectName);
		log.debug(urlStr);
		try {
			return URLHelper.getJsonFromUrl(urlStr);
		} catch (final FileNotFoundException e) {
			urlStr = String.format(reference.getCurseforgeWidgetJsonUrl(), modOrModPack, projectId + "-" + projectName);
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

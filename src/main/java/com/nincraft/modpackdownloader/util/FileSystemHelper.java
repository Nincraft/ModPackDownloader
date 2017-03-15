package com.nincraft.modpackdownloader.util;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.DownloadableFile;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Log4j2
@UtilityClass
public final class FileSystemHelper {

	private static Reference reference = Reference.getInstance();

	public static void createFolder(final String folder) {
		if (folder != null) {
			final File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

	public static void moveFromLocalRepo(final DownloadableFile downloadableFile, final String fileName, boolean downloadToLocalRepo, String modFolder) {
		val newProjectName = getProjectNameOrDefault(downloadableFile.getName());
		String folder = downloadableFile.getFolder();
		if (Strings.isNullOrEmpty(folder)) {
			folder = modFolder;
		}
		try {
			File downloadedFile = getDownloadedFile(fileName, folder);
			if (downloadToLocalRepo) {
				FileUtils.copyFileToDirectory(getLocalFile(fileName, newProjectName), new File(folder));
			} else if (!downloadedFile.exists()) {
				FileUtils.moveFileToDirectory(getLocalFile(fileName, newProjectName), new File(folder), true);
			}
			if (!Strings.isNullOrEmpty(downloadableFile.getRename())) {
				downloadedFile.renameTo(new File(downloadedFile.getParent() + File.separator + downloadableFile.getRename()));
			}
		} catch (final IOException e) {
			log.error(String.format("Could not copy %s from local repo.", newProjectName), e);
		}
	}

	public static boolean isInLocalRepo(final String projectName, final String fileName) {
		return getLocalFile(fileName, getProjectNameOrDefault(projectName)).exists();
	}

	public static File getDownloadedFile(final String fileName, String modFolder) {
		return getDownloadedFile(fileName, null, modFolder);
	}

	private static String getProjectNameOrDefault(final String projectName) {
		return projectName != null ? projectName : "thirdParty";
	}

	public static File getLocalFile(DownloadableFile downloadableFile) {
		return getLocalFile(downloadableFile.getFileName(), downloadableFile.getName());
	}

	private static File getLocalFile(final String fileName, final String newProjectName) {
		return new File(reference.getUserhome() + newProjectName + File.separator + fileName);
	}

	private static File getDownloadedFile(String fileName, String folder, String modFolder) {
		if (folder != null) {
			createFolder(folder);
			return new File(folder + File.separator + fileName);
		} else if (modFolder != null) {
			createFolder(modFolder);
			return new File(modFolder + File.separator + fileName);
		} else {
			return new File(fileName);
		}
	}

	public static void clearCache() {
		File cache = new File(reference.getUserhome());
		log.info("Clearing cache at {}", Reference.userhome);
		try {
			FileUtils.deleteDirectory(cache);
		} catch (IOException e) {
			log.error("Unable to clear cache", e);
		}
	}
}

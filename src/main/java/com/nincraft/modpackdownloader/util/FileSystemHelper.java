package com.nincraft.modpackdownloader.util;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.DownloadableFile;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Log4j2
@UtilityClass
public final class FileSystemHelper {

	public static void createFolder(final String folder) {
		if (folder != null) {
			final File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

	public static void moveFromLocalRepo(final DownloadableFile downloadableFile, final String fileName, boolean downloadToLocalRepo) {
		val newProjectName = getProjectNameOrDefault(downloadableFile.getName());
		String folder = downloadableFile.getFolder();
		if (Strings.isNullOrEmpty(folder)) {
			folder = Arguments.modFolder;
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

	public static File getDownloadedFile(final String fileName) {
		return getDownloadedFile(fileName, null);
	}

	public static String getProjectNameOrDefault(final String projectName) {
		return projectName != null ? projectName : "thirdParty";
	}

	public static File getLocalFile(DownloadableFile downloadableFile) {
		return getLocalFile(downloadableFile.getFileName(), downloadableFile.getName());
	}

	public static File getLocalFile(final String fileName, final String newProjectName) {
		return new File(Reference.userhome + newProjectName + File.separator + fileName);
	}

	public static File getDownloadedFile(String fileName, String folder) {
		if (folder != null) {
			createFolder(folder);
			return new File(folder + File.separator + fileName);
		} else if (Arguments.modFolder != null) {
			createFolder(Arguments.modFolder);
			return new File(Arguments.modFolder + File.separator + fileName);
		} else {
			return new File(fileName);
		}
	}

	public static void clearCache() {
		File cache = new File(Reference.userhome);
		try {
			FileUtils.deleteDirectory(cache);
		} catch (IOException e) {
			log.error("Unable to clear cache", e);
		}
	}

	public static void flushFileWriter(FileWriter file) {
		if (file != null) {
			try {
				file.flush();
			} catch (IOException e) {
				log.error("Unable to flush FileWriter", e);
			}
		}
	}

	public static void closeClosable(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				log.error("Unable to close", e);
			}
		}
	}
}

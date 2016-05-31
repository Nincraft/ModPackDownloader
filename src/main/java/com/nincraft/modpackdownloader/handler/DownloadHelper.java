package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.container.DownloadableFile;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;

@Log4j2
public class DownloadHelper {

	protected static void downloadFile(final DownloadableFile downloadableFile) {
		val decodedFileName = URLHelper.decodeSpaces(downloadableFile.getFileName());

		if (!FileSystemHelper.isInLocalRepo(downloadableFile.getName(), decodedFileName) || Reference.forceDownload) {
			val downloadedFile = FileSystemHelper.getDownloadedFile(decodedFileName, downloadableFile.getFolder());
			try {
				FileUtils.copyURLToFile(new URL(downloadableFile.getDownloadUrl()), downloadedFile);
			} catch (final IOException e) {
				log.error(String.format("Could not download %s.", downloadableFile.getFileName()), e.getMessage());
				Reference.downloadCount++;
				return;
			}

		}
		FileSystemHelper.copyFromLocalRepo(downloadableFile, decodedFileName);
	}
}

package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.container.DownloadableFile;
import com.nincraft.modpackdownloader.status.DownloadStatus;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.IOException;
import java.net.URL;

@Log4j2
public class DownloadHelper {

	/**
	 * Downloads a file to the local cache and moves it to the correct folder. Returns a DownloadStatus
	 *
	 * @param downloadableFile
	 * @return DownloadStatus
	 */
	protected static DownloadStatus downloadFile(final DownloadableFile downloadableFile) {
		if (BooleanUtils.isTrue(downloadableFile.getSkipDownload())) {
			log.trace(String.format("Skipped downloading %s", downloadableFile.getName()));
			return DownloadStatus.SKIPPED;
		}
		val decodedFileName = URLHelper.decodeSpaces(downloadableFile.getFileName());

		if (!FileSystemHelper.isInLocalRepo(downloadableFile.getName(), decodedFileName) || Reference.forceDownload) {
			val downloadedFile = FileSystemHelper.getDownloadedFile(decodedFileName, downloadableFile.getFolder());
			try {
				FileUtils.copyURLToFile(new URL(downloadableFile.getDownloadUrl()), downloadedFile);
			} catch (final IOException e) {
				log.error(String.format("Could not download %s.", downloadableFile.getFileName()), e.getMessage());
				Reference.downloadCount++;
				return DownloadStatus.FAILURE;
			}

		}
		FileSystemHelper.copyFromLocalRepo(downloadableFile, decodedFileName);
		log.info(String.format("Successfully downloaded %s", downloadableFile.getFileName()));
		return DownloadStatus.SUCCESS;
	}
}

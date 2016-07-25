package com.nincraft.modpackdownloader.util;

import com.nincraft.modpackdownloader.container.DownloadableFile;
import com.nincraft.modpackdownloader.status.DownloadStatus;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.IOException;
import java.net.URL;

@Log4j2
public class DownloadHelper {

	/**
	 * Downloads a {@link DownloadableFile} moves it to the correct folder. Downloads to the local cache and then
	 * copies to the download folder. Returns a {@link DownloadStatus}
	 *
	 * @param downloadableFile
	 * @return {@link DownloadStatus}
	 */
	public static DownloadStatus downloadFile(final DownloadableFile downloadableFile) {
		return downloadFile(downloadableFile, true);
	}

	/**
	 * Downloads a {@link DownloadableFile} moves it to the correct folder. Downloads to the local cache if
	 * downloadToLocalRepo is set to true and then copies to the download folder. Returns a {@link DownloadStatus}
	 *
	 * @param downloadableFile
	 * @param downloadToLocalRepo
	 * @return {@link DownloadStatus}
	 */
	public static DownloadStatus downloadFile(final DownloadableFile downloadableFile, boolean downloadToLocalRepo) {
		DownloadStatus status = DownloadStatus.FAILURE;
		if (BooleanUtils.isTrue(downloadableFile.getSkipDownload())) {
			log.trace(String.format("Skipped downloading %s", downloadableFile.getName()));
			return DownloadStatus.SKIPPED;
		}
		val decodedFileName = URLHelper.decodeSpaces(downloadableFile.getFileName());

		if (FileSystemHelper.getDownloadedFile(decodedFileName, downloadableFile.getFolder()).exists() && !Arguments.forceDownload) {
			log.info(String.format("Found %s already downloaded, skipping", decodedFileName));
			return DownloadStatus.SKIPPED;
		}

		if (!FileSystemHelper.isInLocalRepo(downloadableFile.getName(), decodedFileName) || Arguments.forceDownload) {
			val downloadedFile = FileSystemHelper.getLocalFile(downloadableFile);
			try {
				FileUtils.copyURLToFile(new URL(downloadableFile.getDownloadUrl()), downloadedFile);
			} catch (final IOException e) {
				log.error(String.format("Could not download %s.", downloadableFile.getFileName()), e);
				Reference.downloadCount++;
				return status;
			}
			status = DownloadStatus.SUCCESS_DOWNLOAD;
		} else {
			status = DownloadStatus.SUCCESS_CACHE;
		}
		FileSystemHelper.moveFromLocalRepo(downloadableFile, decodedFileName, downloadToLocalRepo);
		log.info(String.format("Successfully %s %s", status, downloadableFile.getFileName()));
		return status;
	}
}

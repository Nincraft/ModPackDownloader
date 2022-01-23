package com.nincraft.modpackdownloader.util;

import com.nincraft.modpackdownloader.container.DownloadableFile;
import com.nincraft.modpackdownloader.status.DownloadStatus;
import com.nincraft.modpackdownloader.summary.DownloadSummarizer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Observable;

import static com.nincraft.modpackdownloader.status.DownloadStatus.*;
import static com.nincraft.modpackdownloader.util.FileSystemHelper.moveFromLocalRepo;
import static java.nio.charset.StandardCharsets.UTF_8;

@Log4j2
public class DownloadHelper extends Observable {

	@Getter
	private final DownloadSummarizer downloadSummarizer = new DownloadSummarizer();
	private final Arguments arguments;

	public DownloadHelper(Arguments arguments) {
		this.addObserver(downloadSummarizer);
		this.arguments = arguments;
	}

	/**
	 * Downloads a {@link DownloadableFile} moves it to the correct folder. Downloads to the local cache and then
	 * copies to the download folder.
	 *
	 * @param downloadableFile a DownloadableFile with a download URL
	 * @return status of the download, failed, skipped, or success
	 */
	public DownloadStatus downloadFile(final DownloadableFile downloadableFile) {
		return downloadFile(downloadableFile, true);
	}

	/**
	 * Downloads a {@link DownloadableFile} moves it to the correct folder. Downloads to the local cache if
	 * downloadToLocalRepo is set to true and then copies to the download folder.
	 *
	 * @param downloadableFile    a DownloadableFile with a download URL
	 * @param downloadToLocalRepo set to true to keep a copy of the DownloadableFile in local cache
	 * @return status of the download, failed, skipped, or success
	 */
	public DownloadStatus downloadFile(final DownloadableFile downloadableFile, boolean downloadToLocalRepo) {
		DownloadStatus status = FAILURE;
		if (BooleanUtils.isTrue(downloadableFile.getSkipDownload())) {
			log.debug("Skipped downloading {}", downloadableFile.getName());
			return notifyStatus(SKIPPED);
		}

        String fileName = downloadableFile.getFileName();

        if (FileSystemHelper.getDownloadedFile(fileName, downloadableFile.getFolder()).exists() && !arguments.isForceDownload()) {
			log.debug("Found {} already downloaded, skipping", fileName);
			return notifyStatus(SKIPPED);
		}

		if (!FileSystemHelper.isInLocalRepo(downloadableFile.getName(), fileName) || arguments.isForceDownload()) {
			val downloadedFile = FileSystemHelper.getLocalFile(downloadableFile);
			try {
				FileUtils.copyURLToFile(new URL(downloadableFile.getDownloadUrl().replace(" ", "%20")), downloadedFile);
			} catch (final IOException e) {
				log.error("Could not download {}.", fileName, e);
				Reference.downloadCount++;
				if ("forge".equals(downloadableFile.getName())) {
					return status;
				}
				return notifyStatus(status);
			}
			status = SUCCESS_DOWNLOAD;
		} else {
			status = SUCCESS_CACHE;
		}
		FileSystemHelper.moveFromLocalRepo(downloadableFile, fileName, downloadToLocalRepo, arguments.getModFolder());
		log.info("Successfully {} {}", status, fileName);
		return notifyStatus(status);
	}

	private DownloadStatus notifyStatus(DownloadStatus status) {
		setChanged();
		notifyObservers(status);
		return status;
	}
}

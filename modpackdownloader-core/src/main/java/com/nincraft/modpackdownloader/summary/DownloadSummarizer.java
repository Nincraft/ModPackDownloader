package com.nincraft.modpackdownloader.summary;

import com.nincraft.modpackdownloader.status.DownloadStatus;
import lombok.extern.log4j.Log4j2;

import java.util.Observable;
import java.util.Observer;

@Log4j2
public class DownloadSummarizer implements Observer {

	private int successTotal = 0;
	private int failureTotal = 0;
	private int skipTotal = 0;

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof DownloadStatus) {
			DownloadStatus downloadStatus = (DownloadStatus) arg;
			switch (downloadStatus) {
				case FAILURE:
					failureTotal++;
					break;
				case SUCCESS_CACHE:
				case SUCCESS_DOWNLOAD:
					successTotal++;
					break;
				case SKIPPED:
					skipTotal++;
					break;
				default:
					break;
			}
		}
	}

	public void summarize() {
		if (successTotal != 0) {
			log.info("Successfully downloaded {} {}", successTotal, getEnding(successTotal));
		}
		if (failureTotal != 0) {
			log.info("Failed to download {} {}", failureTotal, getEnding(failureTotal));
		}
		if (skipTotal != 0) {
			log.info("Skipped downloading {} {}", skipTotal, getEnding(skipTotal));
		}
	}

	private String getEnding(int total) {
		return total == 1 ? "mod" : "mods";
	}
}

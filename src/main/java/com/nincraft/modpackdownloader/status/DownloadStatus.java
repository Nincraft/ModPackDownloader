package com.nincraft.modpackdownloader.status;

public enum DownloadStatus {
	FAILURE(""), SUCCESS_DOWNLOAD("downloaded"), SUCCESS_CACHE("moved from cache"), SKIPPED("");
	String message;

	DownloadStatus(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}
}

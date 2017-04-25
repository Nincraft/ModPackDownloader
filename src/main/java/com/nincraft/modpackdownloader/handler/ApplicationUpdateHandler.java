package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;

@UtilityClass
@Log4j2
public class ApplicationUpdateHandler {

	private Reference reference = Reference.getInstance();

	public void update() {
		JSONObject appJson;
		try {
			appJson = URLHelper.getJsonFromUrl(reference.getUpdateAppUrl());
		} catch (IOException | ParseException e) {
			log.error("Failed to get latest download link, Nincraft server down?", e);
			return;
		}
		val downloadUrl = (String) appJson.get("url");
		val appName = URLHelper
				.decodeSpaces(downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.length()));
		val updatedApp = FileSystemHelper.getDownloadedFile(appName, ".");
		if (updatedApp.exists()) {
			log.info("No new updates found");
			return;
		} else {
			log.info("Update found, downloading " + appName);
		}
		try {
			FileUtils.copyURLToFile(new URL(downloadUrl), updatedApp);
			log.info("Downloaded {}", appName);
		} catch (IOException e) {
			log.error("Failed to download update", e);
		}
	}

}

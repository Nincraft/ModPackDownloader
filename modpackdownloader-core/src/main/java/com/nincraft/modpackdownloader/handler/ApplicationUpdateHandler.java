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
import java.net.URLDecoder;

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

		val appName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
		String decodedAppName = null;
		try {
			decodedAppName = URLDecoder.decode(appName, "UTF-8");
		} catch (IOException e) {
			log.error("Error Decoding App Name: {}", appName, e);
		}

		val updatedApp = FileSystemHelper.getDownloadedFile(decodedAppName, ".");
		if (updatedApp.exists()) {
			log.info("No new updates found");
			return;
		} else {
			log.info("Update found, downloading {}", decodedAppName);
		}
		try {
			FileUtils.copyURLToFile(new URL(downloadUrl), updatedApp);
			log.info("Downloaded {}", decodedAppName);
		} catch (IOException e) {
			log.error("Failed to download update", e);
		}
	}

}

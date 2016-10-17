package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@UtilityClass
@Log4j2
public class ApplicationUpdateHandler {

	public static void update() {
		JSONObject appJson;
		try {
			appJson = URLHelper.getJsonFromUrl(Reference.updateAppURL);
		} catch (IOException | ParseException e) {
			log.error("Failed to get latest download link, Nincraft server down?", e);
			return;
		}
		String downloadUrl = (String) appJson.get("url");
		String appName = URLHelper
				.decodeSpaces(downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.length()));
		File updatedApp = FileSystemHelper.getDownloadedFile(appName, ".");
		if (updatedApp.exists()) {
			log.info("No new updates found");
			return;
		} else {
			log.info("Update found, downloading " + appName);
		}
		try {
			FileUtils.copyURLToFile(new URL(downloadUrl), updatedApp);
			log.info("Downloaded " + appName);
		} catch (IOException e) {
			log.error("Failed to download update", e);
		}
	}

}

package com.nincraft.modpackdownloader.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ApplicationUpdateHandler {

	public static void update() {
		JSONParser parser = new JSONParser();
		JSONObject appJson = null;
		try {
			appJson = (JSONObject) parser
					.parse(new BufferedReader(new InputStreamReader(new URL(Reference.updateAppURL).openStream())));
		} catch (IOException | ParseException e) {
			log.error("Failed to get latest download link, Nincraft server down?", e.getMessage());
			return;
		}
		String downloadUrl = (String) appJson.get("url");
		String appName = URLHelper
				.decodeSpaces(downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.length()));
		File updatedApp = FileSystemHelper.getDownloadedFile(appName);
		if (updatedApp.exists()) {
			log.info("No new updates found");
			return;
		} else {
			log.info("Update found, downloading " + appName);
		}
		try {
			FileUtils.copyURLToFile(new URL(downloadUrl), updatedApp);
		} catch (IOException e) {
			log.error("Failed to download update", e.getMessage());
			return;
		}
		log.info("Downloaded " + appName);
	}

}

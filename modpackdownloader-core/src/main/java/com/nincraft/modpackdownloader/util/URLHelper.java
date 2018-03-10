package com.nincraft.modpackdownloader.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
@Log4j2
public final class URLHelper {
	public static JSONObject getJsonFromUrl(final String url) throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(new BufferedReader(new InputStreamReader(new URL(url).openStream())));
	}

	public static String parseCurseUrl(String projectIdPattern, String projectUrl) {
		String projectIdName = projectUrl.substring(projectUrl.lastIndexOf('/') + 1);
		Pattern pId = Pattern.compile(projectIdPattern);
		Matcher m = pId.matcher(projectIdName);
		if (m.find()) {
			return m.group();
		}
		return null;
	}
}

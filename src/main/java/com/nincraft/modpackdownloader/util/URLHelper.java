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

@UtilityClass
@Log4j2
public final class URLHelper {
	private static final String WHITESPACE = " ";
	private static final String WHITESPACE_ENCODED = "%20";

	public static String encodeSpaces(final String url) {
		return url.replace(WHITESPACE, WHITESPACE_ENCODED);
	}

	public static String decodeSpaces(final String url) {
		return url.replace(WHITESPACE_ENCODED, WHITESPACE);
	}

	public static JSONObject getJsonFromUrl(final String url) throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(new BufferedReader(new InputStreamReader(new URL(url).openStream())));
	}
}

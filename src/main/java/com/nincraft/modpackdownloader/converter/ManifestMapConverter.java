/**
 *
 */
package com.nincraft.modpackdownloader.converter;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nincraft.modpackdownloader.container.Manifest;

import lombok.val;

public class ManifestMapConverter implements IStringConverter<Map<String, Manifest>> {
	private String optionName;

	private static Gson gson = new Gson();
	private static JSONParser parser = new JSONParser();

	public ManifestMapConverter(final String optionName) {
		this.optionName = optionName;
	}

	@Override
	public BiMap<String, Manifest> convert(final String value) {
		val manifests = HashBiMap.<String, Manifest>create();

		try {
			manifests.put(value,
					gson.fromJson(((JSONObject) parser.parse(new FileReader(value))).toString(), Manifest.class));
		} catch (IOException | ParseException e) {
			throw new ParameterException(getErrorString(value, "a Manifest File"));
		}

		return manifests;
	}

	private String getErrorString(final String value, final String to) {
		return "\"" + optionName + "\": couldn't convert \"" + value + "\" to " + to;
	}
}

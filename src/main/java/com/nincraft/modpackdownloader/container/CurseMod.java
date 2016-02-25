package com.nincraft.modpackdownloader.container;

import static com.nincraft.modpackdownloader.util.Reference.COOKIE_TEST_1;
import static com.nincraft.modpackdownloader.util.Reference.CURSEFORGE_BASE_URL;

import org.json.simple.JSONObject;

import com.nincraft.modpackdownloader.util.ModType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CurseMod extends ModContainer {
	private final String projectURL;

	public CurseMod(final JSONObject modJson) {
		super(modJson, ModType.CURSE);
		projectURL = buildProjectUrl(modJson);
	}

	private String buildProjectUrl(final JSONObject modJson) {
		return String.format(CURSEFORGE_BASE_URL + "{}" + COOKIE_TEST_1, modJson.get("projectID"));
	}

	@Override
	String buildUrl(final JSONObject modJson) {
		return String.format(CURSEFORGE_BASE_URL + "{}/files/{}/download", modJson.get("projectID"),
				modJson.get("fileID"));
	}
}

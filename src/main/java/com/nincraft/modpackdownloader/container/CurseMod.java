package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import com.nincraft.modpackdownloader.util.Reference;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CurseMod extends ModContainer {
	private String projectURL;

	public CurseMod(final JSONObject modJson) {
		super(modJson);
		setProjectId((Long) modJson.get("projectID"));
		setFileId((Long) modJson.get("fileID"));
		setProjectURL(buildProjectUrl(modJson));
	}

	private String buildProjectUrl(final JSONObject modJson) {
		return String.format(Reference.CURSEFORGE_BASE_URL + "{}" + Reference.COOKIE_TEST_1, modJson.get("projectID"));
	}

	@Override
	String buildUrl(final JSONObject modJson) {
		return String.format(Reference.CURSEFORGE_BASE_URL + "{}/files/{}/download", modJson.get("projectID"),
				modJson.get("fileID"));
	}
}

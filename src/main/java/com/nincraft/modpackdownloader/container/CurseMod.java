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
		setProjectURL(buildProjectUrl());
	}

	@Override
	public String getDownloadUrl() {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s-%s/files/%s/download", getProjectId(),
				getProjectName(), getFileId());
	}

	String getAlternateDownloadUrl(final JSONObject modJson) {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s/files/%s/download", getProjectName(), getFileId());
	}

	String buildProjectUrl() {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s" + Reference.COOKIE_TEST_1, getProjectId());
	}
}

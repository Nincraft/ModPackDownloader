package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import com.nincraft.modpackdownloader.util.ModType;

import lombok.Data;

@Data
public abstract class ModContainer {
	private String modName;
	private Long projectId;
	private Long fileId;
	private String projectName;
	private String rename;
	private String fileName;
	private String folder;
	private String url;

	public ModContainer(final JSONObject modJson, final ModType type) {
		if (type.equals(ModType.CURSE)) {
			setProjectId((Long) modJson.get("projectID"));
			setFileId((Long) modJson.get("fileID"));
			setUrl(buildUrl(modJson));
		}
	}

	abstract String buildUrl(final JSONObject modJson);
}

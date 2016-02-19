package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import com.nincraft.modpackdownloader.util.ModType;

import lombok.Data;

@Data
public class ModContainer {
	public ModContainer(JSONObject modJson, ModType type) {
		if (type.equals(ModType.CURSE)) {
			setProjectId((Long) modJson.get("projectID"));
			setFileId((Long) modJson.get("fileID"));
		}
	}

	private String modName;
	private Long projectId;
	private Long fileId;
	private String projectName;
	private String rename;
	private String fileName;
	private String folder;
}

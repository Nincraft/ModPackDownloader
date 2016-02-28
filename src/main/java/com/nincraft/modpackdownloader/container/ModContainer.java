package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import lombok.Data;

@Data
public abstract class ModContainer {
	private String modName;
	private String rename;
	private String fileName;
	private String folder;
	private String downloadUrl;

	public ModContainer(final JSONObject modJson) {
		setModName((String) modJson.get("name"));
		setRename((String) modJson.get("rename"));
	}

}

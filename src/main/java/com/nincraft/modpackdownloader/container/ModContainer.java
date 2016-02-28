package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import lombok.Data;

@Data
public abstract class ModContainer implements Cloneable {
	private String modName;
	private String rename;
	private String fileName;
	private String folder;
	private String downloadUrl;
	private String version;

	public ModContainer() {
	}

	public ModContainer(final JSONObject modJson) {
		setModName((String) modJson.get("name"));
		setRename((String) modJson.get("rename"));
		setVersion((String) modJson.get("version"));
	}

	@Override
	public ModContainer clone() throws CloneNotSupportedException {
		return (ModContainer) super.clone();
	}
}

package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import lombok.Data;

@Data
public abstract class Mod implements Cloneable {
	private String modName;
	private String rename;
	private String fileName;
	private String downloadUrl;
	private String version;

	public Mod() {
	}

	public Mod(final JSONObject modJson) {
		setModName((String) modJson.get("name"));
		setRename((String) modJson.get("rename"));
		setVersion((String) modJson.get("version"));
	}

	@Override
	public Mod clone() throws CloneNotSupportedException {
		return (Mod) super.clone();
	}
}

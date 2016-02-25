package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

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
	private String downloadUrl;

	public ModContainer(final JSONObject modJson) {

	}

}

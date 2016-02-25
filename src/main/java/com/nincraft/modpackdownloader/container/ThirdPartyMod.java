package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ThirdPartyMod extends ModContainer {

	private String fileName;

	public ThirdPartyMod(final JSONObject modJson) {
		super(modJson);
		setDownloadUrl(modJson.get("url").toString());
		setProjectName(modJson.get("name").toString());
		setFileName(getRename() == null ? getDownloadUrl().substring(getDownloadUrl().lastIndexOf("/") + 1,
				getDownloadUrl().lastIndexOf(".jar") + 4) : getRename());
	}
}

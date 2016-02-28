package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

public class ThirdPartyMod extends ModContainer {

	public ThirdPartyMod(final JSONObject modJson) {
		super(modJson);
		setDownloadUrl((String) modJson.get("url"));
		setFileName(getDownloadUrl().substring(getDownloadUrl().lastIndexOf("/") + 1,
				getDownloadUrl().lastIndexOf(".jar") + 4));
	}
}

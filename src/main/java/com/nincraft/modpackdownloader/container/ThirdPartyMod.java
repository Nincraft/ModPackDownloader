package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

public class ThirdPartyMod extends ModContainer {

	public ThirdPartyMod() {
	}

	public ThirdPartyMod(final JSONObject modJson) {
		super(modJson);
		setDownloadUrl((String) modJson.get("url"));
		setFileName(buildFileName());
	}

	private String buildFileName() {
		if (getDownloadUrl().endsWith(".jar")) {
			return getDownloadUrl().substring(getDownloadUrl().lastIndexOf("/") + 1,
					getDownloadUrl().lastIndexOf(".jar") + 4);
		}
		return getRename();
	}

	@Override
	public ThirdPartyMod clone() throws CloneNotSupportedException {
		return (ThirdPartyMod) super.clone();
	}
}

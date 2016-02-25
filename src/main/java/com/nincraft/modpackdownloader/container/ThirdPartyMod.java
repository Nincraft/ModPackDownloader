package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ThirdPartyMod extends ModContainer {

	public ThirdPartyMod(final JSONObject modJson) {
		super(modJson);
		setProjectName((String) modJson.get("name"));
		setDownloadUrl((String) modJson.get("url"));
		setFileName(getDownloadUrl().substring(getDownloadUrl().lastIndexOf("/") + 1,
				getDownloadUrl().lastIndexOf(".jar") + 4));
	}
}

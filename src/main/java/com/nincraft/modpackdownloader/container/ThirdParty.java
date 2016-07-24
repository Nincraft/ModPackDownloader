package com.nincraft.modpackdownloader.container;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ThirdParty extends Mod {

	@SerializedName("url")
	@Expose
	private String url;

	public String buildFileName() {
		if (getDownloadUrl().contains(".jar")) {
			return getDownloadUrl().substring(getDownloadUrl().lastIndexOf("/") + 1,
					getDownloadUrl().lastIndexOf(".jar") + 4);
		}
		return getRename();
	}

	public void init() {
		setDownloadUrl(url);
		setFileName(buildFileName());
	}
}
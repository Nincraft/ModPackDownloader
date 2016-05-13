package com.nincraft.modpackdownloader.container;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Generated("org.jsonschema2pojo")
@Getter
public class ModLoader {

	@SerializedName("id")
	@Expose
	private String id;
	@SerializedName("primary")
	@Expose
	private Boolean primary;
	@SerializedName("folder")
	@Expose
	private String folder;
	@SerializedName("downloadInstaller")
	@Expose
	private Boolean downloadInstaller;
	@SerializedName("downloadUniversal")
	@Expose
	private Boolean downloadUniversal;
	@SerializedName("renameInstaller")
	@Expose
	private String renameInstaller;
	@SerializedName("renameUniversal")
	@Expose
	private String renameUniversal;

	public String getRename(boolean downloadInstaller) {
		return downloadInstaller ? getRenameInstaller() : getRenameUniversal();
	}
}
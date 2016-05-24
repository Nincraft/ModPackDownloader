package com.nincraft.modpackdownloader.container;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Generated("org.jsonschema2pojo")
@Getter
@Setter
public class ModLoader extends DownloadableFile {

	@SerializedName("id")
	@Expose
	private String id;
	@SerializedName("primary")
	@Expose
	private Boolean primary;
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
	@SerializedName("release")
	@Expose
	private String release;

	public String getRename(boolean downloadInstaller) {
		return downloadInstaller ? getRenameInstaller() : getRenameUniversal();
	}
}
package com.nincraft.modpackdownloader.container;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
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

	public ModLoader() {
		setName("forge");
	}

	public String getRename(boolean downloadInstaller) {
		return downloadInstaller ? getRenameInstaller() : getRenameUniversal();
	}

	public String getForgeId() {
		return getId().substring(getId().indexOf("-") + 1);
	}
}
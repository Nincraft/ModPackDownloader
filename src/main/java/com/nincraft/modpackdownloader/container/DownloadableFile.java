package com.nincraft.modpackdownloader.container;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public abstract class DownloadableFile {
	@SerializedName("name")
	@Expose
	public String name;
	@SerializedName("rename")
	@Expose
	private String rename;
	@SerializedName("skipDownload")
	@Expose
	private Boolean skipDownload;
	@SerializedName("folder")
	@Expose
	private String folder;
	private String fileName;
	private String downloadUrl;

	public String getFileName() {
		if (!Strings.isNullOrEmpty(getRename())) {
			return getRename();
		}
		return this.fileName;
	}
}

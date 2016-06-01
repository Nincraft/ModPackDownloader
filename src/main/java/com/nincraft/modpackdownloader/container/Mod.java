package com.nincraft.modpackdownloader.container;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public abstract class Mod extends DownloadableFile implements Cloneable {
	private String version;


	public Mod() {
	}

	@Override
	public Mod clone() throws CloneNotSupportedException {
		return (Mod) super.clone();
	}

	public abstract void init();
}

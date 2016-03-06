package com.nincraft.modpackdownloader.container;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public abstract class Mod implements Cloneable {
	private String rename;
	private String fileName;
	private String downloadUrl;
	private String version;
	@SerializedName("name")
	@Expose
	public String name;

	
	public Mod() {
	}

	@Override
	public Mod clone() throws CloneNotSupportedException {
		return (Mod) super.clone();
	}
	
	public abstract void init();
}

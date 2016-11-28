package com.nincraft.modpackdownloader.container;

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

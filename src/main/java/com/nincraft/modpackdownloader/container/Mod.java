package com.nincraft.modpackdownloader.container;

import lombok.Data;

@Data
public abstract class Mod extends DownloadableFile implements Cloneable {
	private String version;


	public Mod() {
	}

	public Mod(Mod mod) {
		super(mod);
		version = mod.version;
	}

	public abstract void init();
}

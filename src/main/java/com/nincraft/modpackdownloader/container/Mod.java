package com.nincraft.modpackdownloader.container;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Mod extends DownloadableFile implements Cloneable {
	private String version;


	Mod() {
	}

	Mod(Mod mod) {
		super(mod);
		version = mod.version;
	}

	public abstract void init();
}

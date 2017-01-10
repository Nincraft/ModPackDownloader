package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.container.Mod;

public interface ModHandler {
	public void downloadMod(final Mod mod);

	public void updateMod(final Mod mod);
}

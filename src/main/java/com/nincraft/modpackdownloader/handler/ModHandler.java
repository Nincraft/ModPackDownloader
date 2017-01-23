package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.container.Mod;

public interface ModHandler {
	void downloadMod(final Mod mod);

	void updateMod(final Mod mod);
}

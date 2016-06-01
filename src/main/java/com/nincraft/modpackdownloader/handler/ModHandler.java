package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.container.Mod;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ModHandler {
	public abstract void downloadMod(final Mod mod);

	public abstract void updateMod(final Mod mod);
}

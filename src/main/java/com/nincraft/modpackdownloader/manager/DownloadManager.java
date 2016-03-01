package com.nincraft.modpackdownloader.manager;

import java.util.List;

import com.nincraft.modpackdownloader.container.ModContainer;
import com.nincraft.modpackdownloader.util.Reference;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DownloadManager {
	public static final void downloadMods(final List<? extends ModContainer> mods) {
		log.trace(String.format("Downloading %s mods...", mods.size()));
		int downloadCount = 1;
		for (val mod : mods) {
			log.info(String.format(Reference.DOWNLOADING_MOD_X_OF_Y, mod.getModName(), downloadCount++,
					Reference.downloadTotal));
			new Thread(() -> {
				Reference.MOD_HANDLERS.get(mod.getClass()).downloadMod(mod);
				Reference.downloadCount++;
				log.info(String.format("Finished downloading %s", mod.getModName()));
			}).start();
		}
		log.trace(String.format("Finished downloading %s mods.", mods.size()));
	}
}

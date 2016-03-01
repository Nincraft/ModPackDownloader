package com.nincraft.modpackdownloader.manager;

import java.util.List;

import com.nincraft.modpackdownloader.container.ModContainer;
import com.nincraft.modpackdownloader.util.Reference;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UpdateManager {
	public static final void updateMods(final List<? extends ModContainer> mods) {
		log.trace(String.format("Updating %s mods...", Reference.updateTotal));
		int updateCount = 1;
		for (val mod : mods) {
			log.info(String.format(Reference.UPDATING_MOD_X_OF_Y, mod.getModName(), updateCount++,
					Reference.updateTotal));
			new Thread(() -> {
				Reference.MOD_HANDLERS.get(mod.getClass()).updateMod(mod);
				Reference.updateCount++;
				log.info(String.format("Finished updating %s", mod.getModName()));
			}).start();
		}
		log.trace(String.format("Finished updating %s mods.", Reference.updateTotal));
	}
}

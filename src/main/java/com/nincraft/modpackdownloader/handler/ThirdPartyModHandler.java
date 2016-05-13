package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.container.Mod;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ThirdPartyModHandler extends ModHandler {

	@Override
	public void downloadMod(final Mod mod) {
		downloadFile(mod, false);
	}

	@Override
	public void updateMod(final Mod mod) {
		log.info("Updating Third Party Mods is not supported.");
	}

}

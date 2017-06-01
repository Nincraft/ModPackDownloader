package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ThirdPartyModHandler implements ModHandler {

	private DownloadHelper downloadHelper;

	public ThirdPartyModHandler(DownloadHelper downloadHelper) {
		this.downloadHelper = downloadHelper;
	}

	@Override
	public void downloadMod(final Mod mod) {
		downloadHelper.downloadFile(mod);
	}

	@Override
	public void updateMod(final Mod mod) {
		log.info("Updating Third Party Mods is not supported.");
	}

}

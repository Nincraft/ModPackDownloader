package com.nincraft.modpackdownloader.handler;

import com.nincraft.modpackdownloader.container.ModContainer;

public class ThirdPartyModHandler extends ModHandler {

	@Override
	public void downloadMod(final ModContainer mod) {
		downloadFile(mod, false);
	}

	@Override
	public void updateMod(final ModContainer mod) {
		// no-op
	}

}

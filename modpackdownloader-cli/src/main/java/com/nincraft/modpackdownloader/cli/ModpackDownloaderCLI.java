package com.nincraft.modpackdownloader.cli;

import com.nincraft.modpackdownloader.ModpackDownloaderManager;
import com.nincraft.modpackdownloader.handler.ApplicationUpdateHandler;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

@Log4j2
public class ModpackDownloaderCLI {

	public static void main(final String[] args) throws InterruptedException {
		log.info("Starting ModpackDownloaderManager with arguments: {}", Arrays.toString(args));
		ModpackDownloaderManager modpackDownloaderManager = new ModpackDownloaderManager(args);

		Arguments arguments = modpackDownloaderManager.getArguments();

		if (arguments.isHelpEnabled()) {
			modpackDownloaderManager.getJCommander().usage();
			return;
		}

		modpackDownloaderManager.init();

		if (arguments.isClearCache()) {
			FileSystemHelper.clearCache();
			return;
		}
		if (arguments.isUpdateApp()) {
			ApplicationUpdateHandler.update();
			return;
		}
		modpackDownloaderManager.processManifests();
	}
}

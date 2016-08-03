package com.nincraft.modpackdownloader;

import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.handler.ApplicationUpdateHandler;
import com.nincraft.modpackdownloader.manager.ModListManager;
import com.nincraft.modpackdownloader.manager.ModPackManager;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ModPackDownloader {
	public static void main(final String[] args) throws InterruptedException {
		Arguments arguments = new Arguments();
		new JCommander(arguments, args);
		defaultArguments();
		if (Arguments.clearCache) {
			FileSystemHelper.clearCache();
			return;
		}
		if (Arguments.updateApp) {
			ApplicationUpdateHandler.update();
			return;
		}

		setupRepo();

		if (Arguments.updateCurseModPack) {
			Arguments.manifestFile = Reference.DEFAULT_MANIFEST_FILE;
			if (ModPackManager.updateModPack()) {
				ModPackManager.checkPastForgeVersion();
				processMods();
				ModPackManager.handlePostDownload();
			}
			return;
		}

		processMods();
	}

	private static void defaultArguments() {
		if (Strings.isNullOrEmpty(Arguments.manifestFile)) {
			log.info("No manifest supplied, using default " + Reference.DEFAULT_MANIFEST_FILE);
			Arguments.manifestFile = Reference.DEFAULT_MANIFEST_FILE;
		}
		if (Strings.isNullOrEmpty(Arguments.modFolder)) {
			log.info("No manifest supplied, using default \"mods\"");
			Arguments.modFolder = "mods";
		}
	}

	private static void setupRepo() {
		log.trace("Setting up local repository...");
		Reference.userhome = System.getProperty("user.home");
		log.debug(String.format("User Home System Property detected as: %s", Reference.userhome));

		Reference.os = System.getProperty("os.name");
		log.debug(String.format("Operating System detected as: %s", Reference.os));

		if (Reference.os.startsWith("Windows")) {
			Reference.userhome += Reference.WINDOWS_FOLDER;
		} else if (Reference.os.startsWith("Mac")) {
			Reference.userhome += Reference.MAC_FOLDER;
		} else {
			Reference.userhome += Reference.OTHER_FOLDER;
		}
		log.debug(String.format("User Home Folder set to: %s", Reference.userhome));

		FileSystemHelper.createFolder(Reference.userhome);

		log.debug("Setting User Agent...");
		System.setProperty("http.agent", "Mozilla/4.0");

		log.trace("Finished setting up local repository.");
	}

	private static void processMods() throws InterruptedException {
		log.trace("Processing Mods...");
		int returnCode = ModListManager.buildModList();
		if (returnCode == -1) {
			return;
		}
		if (Arguments.updateMods) {
			if (Strings.isNullOrEmpty(Arguments.mcVersion)) {
				log.error("No Minecraft version found in manifest file");
				return;
			}

			log.info(String.format("Updating mods with parameters: %s, %s, %s", Arguments.manifestFile,
					Arguments.mcVersion, Arguments.releaseType));
			ModListManager.updateMods();

			waitFinishProcessingMods();

			ModListManager.updateManifest();
			log.info("Finished updating mods.");
		} else {
			log.info(String.format("Downloading mods with parameters: %s, %s", Arguments.manifestFile,
					Arguments.modFolder));
			ModListManager.downloadMods();

			waitFinishProcessingMods();
			log.info("Finished downloading mods.");
		}
		log.trace("Finished Processing Mods.");
	}

	private static void waitFinishProcessingMods() throws InterruptedException {
		while (!ModListManager.getExecutorService().isTerminated()) {
			Thread.sleep(1);
		}
	}
}

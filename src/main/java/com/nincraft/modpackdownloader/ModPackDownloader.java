package com.nincraft.modpackdownloader;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.handler.ApplicationUpdateHandeler;
import com.nincraft.modpackdownloader.manager.ModListManager;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ModPackDownloader {
	public static void main(final String[] args) throws InterruptedException {
		if ("-updateApp".equals(args[0])) {
			ApplicationUpdateHandeler.update();
			return;
		} else if (args.length < 1) {
			log.error("Arguments required: manifest file location");
			return;
		} else {
			processArguments(args);
		}

		setupRepo();

		processMods();
	}

	private static void processArguments(final String[] args) {
		Reference.manifestFile = args[0];

		if (args.length < 2) {
			log.info("No mod folder specified, defaulting to \"mods\"");
			Reference.modFolder = "mods";
		} else {
			Reference.modFolder = args[1];
		}

		if (args.length > 2) {
			for (val arg : args) {
				processArgument(arg);
			}
		}
	}

	private static void processArgument(final String arg) {
		log.trace("Processing given arguments...");
		if (arg.equals("-forceDownload")) {
			Reference.forceDownload = true;
			log.debug("Downloads are now being forced.");
		} else if (arg.equals("-updateMods")) {
			Reference.updateMods = true;
			log.debug("mods will be updated instead of downloaded.");
		} else if (arg.startsWith("-releaseType")) {
			Reference.releaseType = arg.substring(arg.lastIndexOf("=") + 1);
			log.debug(String.format("Checking against mod release type: %s", Reference.releaseType));
		} else if (arg.equals("-generateUrlTxt")) {
			Reference.generateUrlTxt = true;
			log.debug("Mod URL Text files will now be generated.");
		}
		log.trace("Finished processing given arguments.");
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
		if (Reference.updateMods) {
			if (Strings.isNullOrEmpty(Reference.mcVersion)) {
				log.error("No Minecraft version found in manifest file");
				return;
			}

			log.info(String.format("Updating mods with parameters: %s, %s, %s", Reference.manifestFile,
					Reference.mcVersion, Reference.releaseType));
			ModListManager.updateMods();

			while (!(Reference.updateCount >= Reference.updateTotal)) {
				Thread.sleep(1);
			}

			ModListManager.updateManifest();
			log.info("Finished updating mods.");
		} else {
			log.info(String.format("Downloading mods with parameters: %s, %s", Reference.manifestFile,
					Reference.modFolder));
			ModListManager.downloadMods();

			while (!(Reference.downloadCount >= Reference.downloadTotal)) {
				Thread.sleep(1);
			}
			log.info("Finished downloading mods.");
		}
		log.trace("Finished Processing Mods.");
	}
}

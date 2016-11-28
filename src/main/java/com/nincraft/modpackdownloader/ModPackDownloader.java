package com.nincraft.modpackdownloader;

import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nincraft.modpackdownloader.handler.ApplicationUpdateHandler;
import com.nincraft.modpackdownloader.manager.ModPackManager;
import com.nincraft.modpackdownloader.processor.DownloadModsProcessor;
import com.nincraft.modpackdownloader.processor.MergeManifestsProcessor;
import com.nincraft.modpackdownloader.processor.UpdateModsProcessor;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.Arrays;

@UtilityClass
@Log4j2
public class ModPackDownloader {
	public static void main(final String[] args) throws InterruptedException {
		log.info("Starting ModPackDownloader with arguments: " + Arrays.toString(args));
		JCommander jCommander = initArguments(args);

		if (Arguments.helpEnabled) {
			jCommander.usage();
			return;
		}

		// Set default application arguments
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
			if (ModPackManager.updateModPack()) {
				ModPackManager.checkPastForgeVersion();
				ModPackManager.handlePostDownload();
			}
			return;
		}

		processManifests();
	}

	private static void processManifests() throws InterruptedException {
		log.trace("Processing Manifests...");

		updateMods();
		downloadMods();
		mergeManifests();

		log.trace("Finished Processing Manifests.");
	}

	private static void updateMods() throws InterruptedException {
		if (Arguments.updateMods) {
			new UpdateModsProcessor(Arguments.manifests).process();
		}
	}

	private static void downloadMods() throws InterruptedException {
		if (Arguments.downloadMods) {
			new DownloadModsProcessor(Arguments.manifests).process();
		}
	}

	private static void mergeManifests() throws InterruptedException {
		if (Arguments.mergeManifests) {
			new MergeManifestsProcessor(Arguments.manifests).process();
		}
	}

	private static JCommander initArguments(final String[] args) {
		// Initialize application arguments
		return new JCommander(new Arguments(), args);
	}

	private static void defaultArguments() {
		if (CollectionUtils.isEmpty(Arguments.manifests)) {
			log.info(String.format("No manifest supplied, using default %s", Reference.DEFAULT_MANIFEST_FILE));

			Arguments.manifests = Lists.newArrayList(new File(Reference.DEFAULT_MANIFEST_FILE));
		}
		if (Strings.isNullOrEmpty(Arguments.modFolder)) {
			log.info("No output folder supplied, using default \"mods\"");
			Arguments.modFolder = "mods";
		}
		if (!Arguments.downloadMods && !Arguments.updateMods && !Arguments.mergeManifests) {
			Arguments.downloadMods = true;
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
}

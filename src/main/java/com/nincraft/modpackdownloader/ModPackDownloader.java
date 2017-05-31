package com.nincraft.modpackdownloader;

import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nincraft.modpackdownloader.handler.ApplicationUpdateHandler;
import com.nincraft.modpackdownloader.processor.DownloadModpackProcessor;
import com.nincraft.modpackdownloader.processor.DownloadModsProcessor;
import com.nincraft.modpackdownloader.processor.MergeManifestsProcessor;
import com.nincraft.modpackdownloader.processor.UpdateModsProcessor;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;

@UtilityClass
@Log4j2
public class ModPackDownloader {

	private static Reference reference = Reference.getInstance();
	private Arguments arguments;
	private DownloadHelper downloadHelper;

	public static void main(final String[] args) throws InterruptedException {
		log.info("Starting ModPackDownloader with arguments: {}", Arrays.toString(args));
		val jCommander = initArguments(args);

		if (arguments.isHelpEnabled()) {
			jCommander.usage();
			return;
		}

		// Set default application arguments
		defaultArguments();

		setupRepo();

		if (arguments.isClearCache()) {
			FileSystemHelper.clearCache();
			return;
		}
		if (arguments.isUpdateApp()) {
			ApplicationUpdateHandler.update();
			return;
		}

		processManifests();
	}

	private static void processManifests() throws InterruptedException {
		log.trace("Processing Manifests...");
		downloadHelper = new DownloadHelper(arguments);
		downloadModpack();
		updateMods();
		downloadMods();
		mergeManifests();

		log.trace("Finished Processing Manifests.");
	}

	private static void downloadModpack() throws InterruptedException {
		if (StringUtils.isNotBlank(arguments.getUpdateCurseModPack())) {
			new DownloadModpackProcessor(arguments, downloadHelper).process();
			arguments.setDownloadMods(true);
		}
	}

	private static void updateMods() throws InterruptedException {
		if (shouldProcessUpdate()) {
			new UpdateModsProcessor(arguments, downloadHelper).process();
		}
	}

	private static boolean shouldProcessUpdate() {
		return arguments.isUpdateMods() || !StringUtils.isBlank(arguments.getCheckMCUpdate());
	}

	private static void downloadMods() throws InterruptedException {
		if (arguments.isDownloadMods()) {
			new DownloadModsProcessor(arguments, downloadHelper).process();
		}
	}

	private static void mergeManifests() throws InterruptedException {
		if (arguments.isMergeManifests()) {
			new MergeManifestsProcessor(arguments, downloadHelper).process();
		}
	}

	private static JCommander initArguments(final String[] args) {
		// Initialize application arguments
		arguments = new Arguments();
		return new JCommander(arguments, args);
	}

	private static void defaultArguments() {
		if (CollectionUtils.isEmpty(arguments.getManifests())) {
			log.info("No manifest supplied, using default {}", reference.getDefaultManifestFile());
			arguments.setManifests(Lists.newArrayList(new File(reference.getDefaultManifestFile())));
		}
		if (Strings.isNullOrEmpty(arguments.getModFolder())) {
			log.info("No output folder supplied, using default \"mods\"");
			arguments.setModFolder("mods");
		}
		if (noProcessArgsSupplied()) {
			arguments.setDownloadMods(true);
		}
	}

	private static boolean noProcessArgsSupplied() {
		return !arguments.isDownloadMods() && !arguments.isUpdateMods() && !arguments.isMergeManifests() && StringUtils.isBlank(arguments.getCheckMCUpdate());
	}

	private static void setupRepo() {
		log.trace("Setting up local repository...");
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(System.getProperty("user.home"));
		log.debug("User Home System Property detected as: {}", stringBuilder.toString());

		val os = System.getProperty("os.name");
		log.debug("Operating System detected as: {}", os);

		if (os.startsWith("Windows")) {
			stringBuilder.append(reference.getWindowsFolder());
		} else if (os.startsWith("Mac")) {
			stringBuilder.append(reference.getMacFolder());
		} else {
			stringBuilder.append(reference.getOtherFolder());
		}
		reference.setUserhome(stringBuilder.toString());

		log.debug("User Home Folder set to: {}", reference.getUserhome());

		FileSystemHelper.createFolder(reference.getUserhome());

		log.debug("Setting User Agent...");
		System.setProperty("http.agent", "Mozilla/4.0");

		log.trace("Finished setting up local repository.");
	}
}

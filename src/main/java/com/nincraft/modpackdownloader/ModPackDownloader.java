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
		log.info("Starting ModPackDownloader with arguments: " + Arrays.toString(args));
		JCommander jCommander = initArguments(args);

		if (arguments.isHelpEnabled()) {
			jCommander.usage();
			return;
		}

		// Set default application arguments
		defaultArguments();

		if (arguments.isClearCache()) {
			FileSystemHelper.clearCache();
			return;
		}
		if (arguments.isUpdateApp()) {
			ApplicationUpdateHandler.update();
			return;
		}

		setupRepo();

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
		if (arguments.isUpdateCurseModPack()) {
			log.warn("The parameter updateCurseModpack will be changing in the next version. You will need to supply the modpack ID in future versions.");
			new DownloadModpackProcessor(arguments, downloadHelper).process();
			arguments.setDownloadMods(true);
		}
	}

	private static void updateMods() throws InterruptedException {
		if (arguments.isUpdateMods() || !StringUtils.isBlank(arguments.getCheckMCUpdate())) {
			new UpdateModsProcessor(arguments, downloadHelper).process();
		}
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
		Arguments arg = new Arguments();
		JCommander jCommander = new JCommander(arg, args);
		arguments = arg;
		return jCommander;
	}

	private static void defaultArguments() {
		if (CollectionUtils.isEmpty(arguments.getManifests())) {
			log.info(String.format("No manifest supplied, using default %s", reference.getDefaultManifestFile()));

			arguments.setManifests(Lists.newArrayList(new File(reference.getDefaultManifestFile())));
		}
		if (Strings.isNullOrEmpty(arguments.getModFolder())) {
			log.info("No output folder supplied, using default \"mods\"");
			arguments.setModFolder("mods");
		}
		if (!arguments.isDownloadMods() && !arguments.isUpdateMods() && !arguments.isMergeManifests() && StringUtils.isBlank(arguments.getCheckMCUpdate())) {
			arguments.setDownloadMods(true);
		}
	}

	private static void setupRepo() {
		log.trace("Setting up local repository...");
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(System.getProperty("user.home"));
		log.debug(String.format("User Home System Property detected as: %s", stringBuilder.toString()));

		reference.setOs(System.getProperty("os.name"));
		log.debug(String.format("Operating System detected as: %s", reference.getOs()));


		if (reference.getOs().startsWith("Windows")) {
			stringBuilder.append(reference.getWindowsFolder());
		} else if (reference.getOs().startsWith("Mac")) {
			stringBuilder.append(reference.getMacFolder());
		} else {
			stringBuilder.append(reference.getOtherFolder());
		}
		reference.setUserhome(stringBuilder.toString());

		log.debug(String.format("User Home Folder set to: %s", reference.getUserhome()));

		FileSystemHelper.createFolder(reference.getUserhome());

		log.debug("Setting User Agent...");
		System.setProperty("http.agent", "Mozilla/4.0");

		log.trace("Finished setting up local repository.");
	}
}

package com.nincraft.modpackdownloader;

import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nincraft.modpackdownloader.processor.DownloadModpackProcessor;
import com.nincraft.modpackdownloader.processor.DownloadModsProcessor;
import com.nincraft.modpackdownloader.processor.MergeManifestsProcessor;
import com.nincraft.modpackdownloader.processor.UpdateModsProcessor;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * ModpackDownloaderManager is the main class for starting a download/update.
 */
@Log4j2
public class ModpackDownloaderManager {

	private Reference reference = Reference.getInstance();
	private DownloadHelper downloadHelper;
	@Getter
	private Arguments arguments;
	@Getter
	private JCommander jCommander;

	/**
	 * Initializes arguments and jCommander
	 *
	 * @param args String[] of arguments for execution. See {@link Arguments} for all current parameters
	 */
	public ModpackDownloaderManager(String[] args){
		arguments = new Arguments();
		jCommander = initArguments(args);
	}

	/**
	 * Starts processing the manifest files passed in. This includes downloading a modpack, updating the mods in a
	 * manifest, downloading individual mods, and merging all manifests.
	 * @throws InterruptedException exception is thrown if a processor is interrupted
	 */
	public void processManifests() throws InterruptedException {
		log.trace("Processing Manifests...");
		downloadHelper = new DownloadHelper(arguments);
		downloadModpack(arguments);
		updateMods(arguments);
		downloadMods(arguments);
		mergeManifests(arguments);

		log.trace("Finished Processing Manifests.");
	}

	private void downloadModpack(Arguments arguments) throws InterruptedException {
		if (StringUtils.isNotBlank(arguments.getUpdateCurseModPack())) {
			new DownloadModpackProcessor(arguments, downloadHelper).process();
			arguments.setDownloadMods(true);
		}
	}

	private void updateMods(Arguments arguments) throws InterruptedException {
		if (shouldProcessUpdate(arguments)) {
			new UpdateModsProcessor(arguments, downloadHelper).process();
		}
	}

	private boolean shouldProcessUpdate(Arguments arguments) {
		return arguments.isUpdateMods() || !StringUtils.isBlank(arguments.getCheckMCUpdate());
	}

	private void downloadMods(Arguments arguments) throws InterruptedException {
		if (arguments.isDownloadMods()) {
			new DownloadModsProcessor(arguments, downloadHelper).process();
		}
	}

	private void mergeManifests(Arguments arguments) throws InterruptedException {
		if (arguments.isMergeManifests()) {
			new MergeManifestsProcessor(arguments, downloadHelper).process();
		}
	}

	private void defaultArguments(Arguments arguments) {
		if (CollectionUtils.isEmpty(arguments.getManifests())) {
			log.info("No manifest supplied, using default {}", reference.getDefaultManifestFile());
			arguments.setManifests(Lists.newArrayList(new File(reference.getDefaultManifestFile())));
		}
		if (Strings.isNullOrEmpty(arguments.getModFolder())) {
			log.info("No output folder supplied, using default \"mods\"");
			arguments.setModFolder("mods");
		}
		if (noProcessArgsSupplied(arguments)) {
			arguments.setDownloadMods(true);
		}
	}

	private boolean noProcessArgsSupplied(Arguments arguments) {
		return !arguments.isDownloadMods() && !arguments.isUpdateMods() && !arguments.isMergeManifests()
				&& StringUtils.isBlank(arguments.getCheckMCUpdate());
	}

	private void setupRepo() {
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

	/**
	 * Initializes the default arguments and sets up the local repo if needed
	 */
	public void init() {
		defaultArguments(arguments);
		setupRepo();
	}

	private JCommander initArguments(final String[] args) {
		// Initialize application arguments
		arguments = new Arguments();
		return new JCommander(arguments, args);
	}
}

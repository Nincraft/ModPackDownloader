package com.nincraft.modpackdownloader.processor;

import com.google.gson.GsonBuilder;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.handler.ForgeHandler;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class UpdateModsProcessor extends AbstractProcessor {
	public UpdateModsProcessor(final List<File> manifestFiles) {
		super(manifestFiles);
	}

	@Override
	protected void init(final Map<File, Manifest> manifestMap) {
		// no-op
	}

	@Override
	protected void preprocess(final Entry<File, Manifest> manifestEntry) {
		backupManifest(manifestEntry.getKey(), manifestEntry.getValue());
	}

	@Override
	protected void process(final Entry<File, Manifest> manifestEntry) {
		updateMods(manifestEntry.getValue(), buildModList(manifestEntry.getKey(), manifestEntry.getValue()));
	}

	@Override
	protected void postProcess(final Entry<File, Manifest> manifestEntry) {
		updateManifest(manifestEntry.getKey(), manifestEntry.getValue());
	}

	private void backupManifest(final File manifestFile, final Manifest manifest) {
		if (CollectionUtils.isNotEmpty(manifest.getCurseFiles())) {
			backupCurseManifest(manifestFile);
		}
	}

	public static void backupCurseManifest(final File manifestFile) {
		try {
			FileUtils.copyFile(manifestFile, new File(manifestFile.getAbsolutePath() + ".bak"), true);
		} catch (IOException e) {
			log.error("Could not backup Curse manifest file", e);
		}
	}

	public static void updateMods(final Manifest manifest, final List<Mod> modList) {
		if (!manifest.getBatchAddCurse().isEmpty()) {
			log.info("Found batch add for Curse");
			addBatch(manifest, modList);
		}

		Reference.updateTotal = modList.size();

		Runnable forgeThread = new Thread(() ->
			manifest.getMinecraft().setModLoaders(
					ForgeHandler.updateForge(manifest.getMinecraftVersion(), manifest.getMinecraft().getModLoaders())));

		setExecutorService(Executors.newFixedThreadPool(Reference.updateTotal + 1));

		getExecutorService().execute(forgeThread);

		log.trace(String.format("Updating %s mods...", Reference.updateTotal));

		int updateCount = 1;
		for (val mod : modList) {
			log.info(String.format(Reference.UPDATING_MOD_X_OF_Y, mod.getName(), updateCount++, Reference.updateTotal));
			Runnable modUpdate = new Thread(() -> {
				MOD_HANDLERS.get(mod.getClass()).updateMod(mod);
				Reference.updateCount++;
				log.info(String.format("Finished updating %s", mod.getName()));
			});
			getExecutorService().execute(modUpdate);
		}
		getExecutorService().shutdown();
		log.trace(String.format("Finished updating %s mods.", Reference.updateTotal));
	}

	private static void addBatch(final Manifest manifestFile, final List<Mod> modList) {
		CurseFile curseFile;
		String projectIdPattern = "(\\d)+";
		String projectNamePattern = "(((?:[a-z][a-z]+))(-)?)+";
		for (String projectUrl : manifestFile.getBatchAddCurse()) {
			String projectIdName = projectUrl.substring(projectUrl.lastIndexOf('/') + 1);
			Pattern pId = Pattern.compile(projectIdPattern);
			Matcher m = pId.matcher(projectIdName);
			String projectId = null;
			if (m.find()) {
				projectId = m.group();
			}

			String projectName = null;

			pId = Pattern.compile(projectNamePattern);
			m = pId.matcher(projectIdName);
			if (m.find()) {
				projectName = m.group();
			}

			if (projectId != null && projectName != null) {
				curseFile = new CurseFile(projectId, projectName);
				curseFile.init();
				log.info(String.format("Adding %s from batch add", curseFile.getName()));
				modList.add(curseFile);
				manifestFile.getCurseFiles().add(curseFile);
			} else {
				log.warn(String.format("Unable to add %s from batch add", projectUrl));
			}
		}
	}

	public static void updateManifest(final File file, final Manifest manifest) {
		log.info("Updating Manifest File...");
		// Sort Mod Lists
		manifest.getCurseFiles().sort(MOD_COMPARATOR);
		manifest.getThirdParty().sort(MOD_COMPARATOR);

		// Clean up Empty Lists
		cleanupLists(manifest);

		// Dump Manifest to file
		val prettyGson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation()
				.disableHtmlEscaping().create();
		try (val fileWriter = new FileWriter(file)) {
			fileWriter.write(prettyGson.toJson(manifest));
		} catch (final IOException e) {
			log.error(e);
		}
	}

	public static void cleanupLists(final Manifest manifest) {

		// Clean up Empty Lists
		if (manifest.getCurseFiles().isEmpty()) {
			manifest.setCurseFiles(null);
		}
		if (manifest.getThirdParty().isEmpty()) {
			manifest.setThirdParty(null);
		}
		if (manifest.getMinecraft().getModLoaders().isEmpty()) {
			manifest.getMinecraft().setModLoaders(null);
		}

		// Always Clean up Batch Add
		manifest.setBatchAddCurse(null);
	}
}

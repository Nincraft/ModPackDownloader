package com.nincraft.modpackdownloader.processor;

import com.google.common.collect.Lists;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.handler.ForgeHandler;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import static java.util.Collections.singletonList;

@Log4j2
public class DownloadModsProcessor extends AbstractProcessor {
	private final List<Mod> modList = Lists.newArrayList();
	private Reference reference = Reference.getInstance();

	public DownloadModsProcessor(Arguments arguments, DownloadHelper downloadHelper) {
		super(arguments, downloadHelper);
	}

	private void downloadMods(final Manifest manifest) {
		setExecutorService(Executors.newFixedThreadPool(arguments.getMaxDownloadThreads() > 0 ? arguments.getMaxDownloadThreads() : modList.size() + 1));

		val modLoader = manifest.getModLoader();
		if (modLoader != null) {
		    val forgeHandler = new ForgeHandler(arguments, downloadHelper);

		    Runnable forgeThread = new Thread(() -> forgeHandler.downloadForge(manifest.getMinecraftVersion(), singletonList(modLoader)));

		    getExecutorService().execute(forgeThread);
        }

		/*val minecraft = manifest.getMinecraft();
		if (minecraft != null) {
			ForgeHandler forgeHandler = new ForgeHandler(arguments, downloadHelper);
			Runnable forgeThread = new Thread(() -> forgeHandler.downloadForge(manifest.getMinecraftVersion(), minecraft.getModLoaders()));

			getExecutorService().execute(forgeThread);
		}*/

		log.trace("Downloading {} mods...", modList.size());
		int downloadCount = 1;
		for (val mod : modList) {
			log.debug(reference.getDownloadingModXOfY(), mod.getName(), downloadCount++, Reference.downloadTotal);

			Runnable modDownload = new Thread(() -> {
				modHandlerHashMap.get(mod.getClass()).downloadMod(mod);
				Reference.downloadCount++;
				log.trace("Finished downloading {}", mod.getName());
			});
			getExecutorService().execute(modDownload);
		}
		getExecutorService().shutdown();
		log.trace("Finished downloading {} mods.", modList.size());
	}

	@Override
	protected void init(final Map<File, Manifest> manifestMap) {
		for (val manifestEntry : manifestMap.entrySet()) {
			modList.addAll(buildModList(manifestEntry.getKey(), manifestEntry.getValue()));
		}

		Reference.downloadTotal = modList.size();
		log.debug("A total of {} mods will be downloaded.", Reference.downloadTotal);
	}

	@Override
	protected boolean process(final Entry<File, Manifest> manifestEntry) {
		downloadMods(manifestEntry.getValue());
		return true;
	}

	@Override
	protected boolean postProcess(final Entry<File, Manifest> manifestEntry) {
		moveOverrides(manifestEntry.getValue());
		downloadHelper.getDownloadSummarizer().summarize();
		return true;
	}

	private void moveOverrides(Manifest manifest) {
		if (!StringUtils.isBlank(manifest.getOverrides())) {
			try {
				File overridesDirectory = new File(manifest.getOverrides());
				if (overridesDirectory.exists()) {
					FileUtils.copyDirectory(overridesDirectory, new File("."));
					FileUtils.deleteDirectory(overridesDirectory);
					log.debug("Successfully moved overrides: {}", manifest.getOverrides());
				}
			} catch (IOException e) {
				log.error("Unable to move {} folder", manifest.getOverrides(), e);
			}
		}
	}
}

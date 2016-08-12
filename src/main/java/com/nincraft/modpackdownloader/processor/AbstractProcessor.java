/**
 *
 */
package com.nincraft.modpackdownloader.processor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.beust.jcommander.internal.Maps;
import com.google.gson.Gson;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.container.ThirdParty;
import com.nincraft.modpackdownloader.handler.CurseFileHandler;
import com.nincraft.modpackdownloader.handler.ModHandler;
import com.nincraft.modpackdownloader.handler.ThirdPartyModHandler;
import com.nincraft.modpackdownloader.manager.ModListManager;
import com.nincraft.modpackdownloader.util.Arguments;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractProcessor {
	@Getter
	public static ExecutorService executorService;

	protected static final Map<Class<? extends Mod>, ModHandler> MOD_HANDLERS = Maps.newHashMap();

	protected Map<File, Manifest> manifestMap = Maps.newHashMap();
	private static Gson gson = new Gson();

	static {
		log.trace("Registering various mod type handlers...");
		MOD_HANDLERS.put(CurseFile.class, new CurseFileHandler());
		MOD_HANDLERS.put(ThirdParty.class, new ThirdPartyModHandler());
		log.trace("Finished registering various mod type handlers.");
	}

	public AbstractProcessor(final List<File> manifestFiles) {
		buildManifestList(manifestFiles);
	}

	private final void buildManifestList(final List<File> manifestFiles) {
		for (val manifestFile : manifestFiles) {
			manifestMap.put(manifestFile, buildManifest(manifestFile));
		}
	}

	private Manifest buildManifest(final File manifestFile) {
		JSONObject jsonLists = null;
		try {
			jsonLists = (JSONObject) new JSONParser().parse(new FileReader(manifestFile));
		} catch (IOException | ParseException e) {
			log.error(e);
			return null;
		}

		return gson.fromJson(jsonLists.toString(), Manifest.class);
	}

	public void process() throws InterruptedException {
		init(manifestMap);

		for (val manifestEntry : manifestMap.entrySet()) {
			preprocess(manifestEntry);

			process(manifestEntry);
			waitFinishProcessingMods();

			postProcess(manifestEntry);
		}
	}

	protected abstract void init(Map<File, Manifest> manifestMap);

	protected abstract void preprocess(Entry<File, Manifest> manifest);

	protected abstract void process(Entry<File, Manifest> manifest) throws InterruptedException;

	protected abstract void postProcess(Entry<File, Manifest> manifest);

	public static List<Mod> buildModList(final File file, final Manifest manifest) {
		log.trace("Building Mod List...");

		val modList = new ArrayList<Mod>();
		if (manifest.getMinecraftVersion() != null) {
			Arguments.mcVersion = manifest.getMinecraftVersion();
		}

		modList.addAll(manifest.getCurseFiles());
		modList.addAll(manifest.getThirdParty());

		modList.forEach(Mod::init);

		Collections.sort(modList, ModListManager.compareMods);

		log.trace("Finished Building Mod List.");
		return modList;
	}

	protected static void waitFinishProcessingMods() throws InterruptedException {
		while (!getExecutorService().isTerminated()) {
			Thread.sleep(1);
		}
	}
}

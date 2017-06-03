package com.nincraft.modpackdownloader.processor;

import com.google.gson.Gson;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.container.ThirdParty;
import com.nincraft.modpackdownloader.handler.CurseFileHandler;
import com.nincraft.modpackdownloader.handler.ModHandler;
import com.nincraft.modpackdownloader.handler.ThirdPartyModHandler;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public abstract class AbstractProcessor {

	static final Comparator<Mod> MOD_COMPARATOR = Comparator.comparing(mod -> mod.getName().toLowerCase());
	static final Map<Class<? extends Mod>, ModHandler> MOD_HANDLERS = new HashMap<>();
	@Getter
	@Setter
	private static ExecutorService executorService;
	private static Gson gson = new Gson();
	Map<File, Manifest> manifestMap = new HashMap<>();
	Arguments arguments;
	DownloadHelper downloadHelper;

	AbstractProcessor(Arguments arguments, DownloadHelper downloadHelper) {
		MOD_HANDLERS.put(CurseFile.class, new CurseFileHandler(arguments, downloadHelper));
		MOD_HANDLERS.put(ThirdParty.class, new ThirdPartyModHandler(downloadHelper));
		this.arguments = arguments;
		this.downloadHelper = downloadHelper;
		buildManifestList(arguments.getManifests());
	}

	private void buildManifestList(final List<File> manifestFiles) {
		for (val manifestFile : manifestFiles) {
			manifestMap.put(manifestFile, buildManifest(manifestFile));
		}
	}

	private Manifest buildManifest(final File manifestFile) {
		JSONObject jsonLists;
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

	boolean preprocess(Entry<File, Manifest> manifest) {
		return true;
	}

	boolean process(Entry<File, Manifest> manifest) throws InterruptedException {
		return true;
	}

	boolean postProcess(Entry<File, Manifest> manifest) {
		return true;
	}

	List<Mod> buildModList(final File file, final Manifest manifest) {
		log.trace("Building Mod List...");

		val modList = new ArrayList<Mod>();
		if (manifest.getMinecraftVersion() != null && StringUtils.isBlank(arguments.getCheckMCUpdate())) {
			arguments.setMcVersion(manifest.getMinecraftVersion());
		}

		modList.addAll(manifest.getCurseFiles());
		modList.addAll(manifest.getThirdParty());

		modList.forEach(Mod::init);

		modList.sort(MOD_COMPARATOR);

		log.trace("Finished Building Mod List.");
		return modList;
	}

	private void waitFinishProcessingMods() throws InterruptedException {
		getExecutorService().awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}
}

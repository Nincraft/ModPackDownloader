package com.nincraft.modpackdownloader.manager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nincraft.modpackdownloader.container.*;
import com.nincraft.modpackdownloader.handler.ForgeHandler;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nincraft.modpackdownloader.handler.CurseModHandler;
import com.nincraft.modpackdownloader.handler.ModHandler;
import com.nincraft.modpackdownloader.handler.ThirdPartyModHandler;
import com.nincraft.modpackdownloader.util.Reference;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ModListManager {
	private static final List<Mod> MOD_LIST = Lists.newArrayList();

	public static final Map<Class<? extends Mod>, ModHandler> MOD_HANDLERS = Maps.newHashMap();

	@Getter
	public static ExecutorService executorService;

	private static Manifest manifestFile;
	private static Gson gson = new Gson();

	private static Comparator<Mod> compareMods = new Comparator<Mod>() {
		@Override
		public int compare(Mod mod1, Mod mod2) {
			return mod1.getName().toLowerCase().compareTo(mod2.getName().toLowerCase());
		}
	};

	static {
		log.trace("Registering various mod type handlers...");
		MOD_HANDLERS.put(CurseFile.class, new CurseModHandler());
		MOD_HANDLERS.put(ThirdParty.class, new ThirdPartyModHandler());
		log.trace("Finished registering various mod type handlers.");
	}

	public static int buildModList() {
		log.trace("Building Mod List...");
		JSONObject jsonLists = null;
		try {
			jsonLists = (JSONObject) new JSONParser().parse(new FileReader(Reference.manifestFile));
		} catch (IOException | ParseException e) {
			log.error(e.getMessage());
			return -1;
		}

		manifestFile = gson.fromJson(jsonLists.toString(), Manifest.class);
		if (!manifestFile.getCurseFiles().isEmpty()) {
			backupCurseManifest();
		}
		Reference.mcVersion = manifestFile.getMinecraftVersion();

		MOD_LIST.addAll(manifestFile.getCurseFiles());
		MOD_LIST.addAll(manifestFile.getThirdParty());
		Reference.updateTotal = Reference.downloadTotal = MOD_LIST.size();
		log.debug(String.format("A total of %s mods will be %s.", Reference.downloadTotal,
				Reference.updateMods ? "updated" : "downloaded"));

		MOD_LIST.forEach(Mod::init);

		Collections.sort(MOD_LIST, compareMods);

		log.trace("Finished Building Mod List.");
		return 0;
	}

	private static void backupCurseManifest() {
		try {
			FileUtils.copyFile(new File(Reference.manifestFile), new File(Reference.manifestFile + ".bak"), true);
		} catch (IOException e) {
			log.error("Could not backup Curse manifest file", e.getMessage());
		}
	}

	public static final void downloadMods() {
		executorService = Executors.newFixedThreadPool(MOD_LIST.size() + 1);
		Runnable forgeThread = new Thread(() -> {
			ForgeHandler.downloadForge(manifestFile.getMinecraftVersion(), manifestFile.getMinecraft().getModLoaders());
		});

		executorService.execute(forgeThread);

		log.trace(String.format("Downloading %s mods...", MOD_LIST.size()));
		int downloadCount = 1;
		for (val mod : MOD_LIST) {
			log.info(String.format(Reference.DOWNLOADING_MOD_X_OF_Y, mod.getName(), downloadCount++,
					Reference.downloadTotal));

			Runnable modDownload = new Thread(() -> {
				MOD_HANDLERS.get(mod.getClass()).downloadMod(mod);
				Reference.downloadCount++;
				log.info(String.format("Finished downloading %s", mod.getName()));
			});
			executorService.execute(modDownload);
		}
		executorService.shutdown();
		log.trace(String.format("Finished downloading %s mods.", MOD_LIST.size()));
	}

	public static final void updateMods() {
		if (!manifestFile.getBatchAddCurse().isEmpty()) {
			log.info("Found batch add for Curse");
			addBatch();
			Reference.updateTotal = MOD_LIST.size();
		}
		log.trace(String.format("Updating %s mods...", Reference.updateTotal));
		executorService = Executors.newFixedThreadPool(MOD_LIST.size());
		int updateCount = 1;
		for (val mod : MOD_LIST) {
			log.info(String.format(Reference.UPDATING_MOD_X_OF_Y, mod.getName(), updateCount++, Reference.updateTotal));
			Runnable modUpdate = new Thread(() -> {
				MOD_HANDLERS.get(mod.getClass()).updateMod(mod);
				Reference.updateCount++;
				log.info(String.format("Finished updating %s", mod.getName()));
			});
			executorService.execute(modUpdate);
		}
		executorService.shutdown();
		log.trace(String.format("Finished updating %s mods.", Reference.updateTotal));
	}

	private static void addBatch() {
		CurseFile curseFile;
		String projectIdPattern = "(\\d)+";
		String projectNamePattern = "(((?:[a-z][a-z]+))(-)?)+";
		for (String projectUrl : manifestFile.getBatchAddCurse()) {
			Pattern pId = Pattern.compile(projectIdPattern);
			Matcher m = pId.matcher(projectUrl);
			String projectId = null;
			if (m.find()) {
				projectId = m.group();
			}

			String projectName = null;

			pId = Pattern.compile(projectNamePattern);
			m = pId.matcher(projectUrl);
			if (m.find()) {
				projectName = m.group();
			}

			if (projectId != null && projectName != null) {
				curseFile = new CurseFile(projectId, projectName);
				curseFile.init();
				log.info(String.format("Adding %s from batch add", projectName));
				MOD_LIST.add(curseFile);
				manifestFile.getCurseFiles().add(curseFile);
			} else {
				log.warn(String.format("Unable to add %s from batch add", projectUrl));
			}
		}
	}

	public static void updateManifest() {
		log.info("Updating Manifest File...");
		try {
			manifestFile.getCurseFiles().sort(compareMods);
			manifestFile.getThirdParty().sort(compareMods);
			nullEmptyLists();
			removeBatchAdd();
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation()
					.disableHtmlEscaping().create();
			val file = new FileWriter(Reference.manifestFile);
			file.write(prettyGson.toJson(manifestFile));
			file.flush();
			file.close();
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
	}

	private static void removeBatchAdd() {
		manifestFile.setBatchAddCurse(null);
	}

	private static void nullEmptyLists() {
		if (manifestFile.getCurseFiles().isEmpty()) {
			manifestFile.setCurseFiles(null);
		}
		if (manifestFile.getThirdParty().isEmpty()) {
			manifestFile.setThirdParty(null);
		}
		if (manifestFile.getMinecraft().getModLoaders().isEmpty()) {
			manifestFile.getMinecraft().setModLoaders(null);
		}
		if (manifestFile.getBatchAddCurse().isEmpty()) {
			manifestFile.setBatchAddCurse(null);
		}
	}
}

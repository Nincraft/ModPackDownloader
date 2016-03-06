package com.nincraft.modpackdownloader.manager;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.container.ThirdParty;
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

	private static boolean processedCurseMods = false;
	private static boolean processedThirdPartyMods = false;
	private static Manifest manifestFile;
	private static Gson gson = new Gson();

	static {
		log.trace("Registering various mod type handlers...");
		MOD_HANDLERS.put(CurseFile.class, new CurseModHandler());
		MOD_HANDLERS.put(ThirdParty.class, new ThirdPartyModHandler());
		log.trace("Finished registering various mod type handlers.");
	}

	public static void buildModList() {
		log.trace("Building Mod List...");
		JSONObject jsonLists = null;
		try {
			jsonLists = (JSONObject) new JSONParser().parse(new FileReader(Reference.manifestFile));
		} catch (IOException | ParseException e) {
			log.error(e.getMessage());
			return;
		}

		manifestFile = gson.fromJson(jsonLists.toString(), Manifest.class);

		MOD_LIST.addAll(manifestFile.getCurseFiles());
		processedCurseMods = true;

		MOD_LIST.addAll(manifestFile.getThirdParty());
		processedThirdPartyMods = true;
		
		Reference.updateTotal = Reference.downloadTotal = MOD_LIST.size();
		log.debug(String.format("A total of %s mods will be %s.", Reference.downloadTotal,
				Reference.updateMods ? "updated" : "downloaded"));

		Comparator<Mod> compareMods = new Comparator<Mod>() {
			@Override
			public int compare(Mod mod1, Mod mod2) {
				return mod1.getName().toLowerCase().compareTo(mod2.getName().toLowerCase());
			}
		};
		
		Collections.sort(MOD_LIST, compareMods);
		
		MOD_LIST.forEach(Mod::init);
		
		log.trace("Finished Building Mod List.");
	}

	public static Optional<JSONArray> getCurseModList(final JSONObject jsonList) {
		return Optional.ofNullable(
				(JSONArray) (jsonList.containsKey("curseFiles") ? jsonList.get("curseFiles") : jsonList.get("files")));
	}

	public static Optional<JSONArray> getThirdPartyModList(final JSONObject jsonLists) {
		return Optional.ofNullable((JSONArray) jsonLists.get("thirdParty"));
	}

	public static final void downloadMods() {
		log.trace(String.format("Downloading %s mods...", MOD_LIST.size()));
		int downloadCount = 1;
		for (val mod : MOD_LIST) {
			log.info(String.format(Reference.DOWNLOADING_MOD_X_OF_Y, mod.getName(), downloadCount++,
					Reference.downloadTotal));
			new Thread(() -> {
				MOD_HANDLERS.get(mod.getClass()).downloadMod(mod);
				Reference.downloadCount++;
				log.info(String.format("Finished downloading %s", mod.getName()));
			}).start();
		}
		log.trace(String.format("Finished downloading %s mods.", MOD_LIST.size()));
	}

	public static final void updateMods() {
		log.trace(String.format("Updating %s mods...", Reference.updateTotal));
		int updateCount = 1;
		for (val mod : MOD_LIST) {
			log.info(String.format(Reference.UPDATING_MOD_X_OF_Y, mod.getName(), updateCount++,
					Reference.updateTotal));
			new Thread(() -> {
				MOD_HANDLERS.get(mod.getClass()).updateMod(mod);
				Reference.updateCount++;
				log.info(String.format("Finished updating %s", mod.getName()));
			}).start();
		}
		log.trace(String.format("Finished updating %s mods.", Reference.updateTotal));
	}

	public static void updateManifest() {
		log.info("Updating Manifest File...");
		try {
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
}
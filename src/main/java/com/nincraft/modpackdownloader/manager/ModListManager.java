package com.nincraft.modpackdownloader.manager;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nincraft.modpackdownloader.container.CurseMod;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.container.ThirdPartyMod;
import com.nincraft.modpackdownloader.handler.CurseModHandler;
import com.nincraft.modpackdownloader.handler.ModHandler;
import com.nincraft.modpackdownloader.handler.ThirdPartyModHandler;
import com.nincraft.modpackdownloader.mapper.CurseModMapper;
import com.nincraft.modpackdownloader.mapper.ThirdPartyMapper;
import com.nincraft.modpackdownloader.util.Reference;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ModListManager {
	private static final List<Mod> MOD_LIST = Lists.newArrayList();

	public static final Map<Class<? extends Mod>, ModHandler> MOD_HANDLERS = Maps.newHashMap();

	private static boolean processedCurseMods = false;
	private static boolean processedThirdPartyMods = false;

	static {
		log.trace("Registering various mod type handlers...");
		MOD_HANDLERS.put(CurseMod.class, new CurseModHandler());
		MOD_HANDLERS.put(ThirdPartyMod.class, new ThirdPartyModHandler());
		log.trace("Finished registering various mod type handlers.");
	}

	public static void addMod(final Mod mod) {
		MOD_LIST.add(mod);
	}

	public static void clearModList() {
		MOD_LIST.clear();
	}

	public static void sortModList() {
		MOD_LIST.sort((mod1, mod2) -> mod1.getModName().compareToIgnoreCase(mod2.getModName()));
	}

	public static int getModListCount() {
		return MOD_LIST.size();
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

		val curseMods = getCurseModList(jsonLists);
		if (curseMods != null) {
			for (val curseMod : curseMods) {
				val mod = new CurseMod((JSONObject) curseMod);
				addMod(mod);
				processedCurseMods = true;
				log.debug(String.format("Curse Mod '%s' found.", mod.getModName()));
			}
		}

		val thirdPartyMods = getThirdPartyModList(jsonLists);
		if (thirdPartyMods != null) {
			for (val thirdPartyMod : thirdPartyMods) {
				val mod = new ThirdPartyMod((JSONObject) thirdPartyMod);
				addMod(mod);
				processedThirdPartyMods = true;
				log.debug(String.format("Third Party Mod '%s' found.", mod.getModName()));
			}
		}

		Reference.updateTotal = Reference.downloadTotal = getModListCount();
		log.debug(String.format("A total of %s mods will be %s.", Reference.downloadTotal,
				Reference.updateMods ? "updated" : "downloaded"));

		sortModList();
		log.trace("Finished Building Mod List.");
	}

	public static JSONArray getCurseModList(final JSONObject jsonList) {
		return (JSONArray) (jsonList.containsKey("curseFiles") ? jsonList.get("curseFiles") : jsonList.get("files"));
	}

	public static JSONArray getThirdPartyModList(final JSONObject jsonLists) {
		return (JSONArray) jsonLists.get("thirdParty");
	}

	public static final void downloadMods() {
		log.trace(String.format("Downloading %s mods...", MOD_LIST.size()));
		int downloadCount = 1;
		for (val mod : MOD_LIST) {
			log.info(String.format(Reference.DOWNLOADING_MOD_X_OF_Y, mod.getModName(), downloadCount++,
					Reference.downloadTotal));
			new Thread(() -> {
				MOD_HANDLERS.get(mod.getClass()).downloadMod(mod);
				Reference.downloadCount++;
				log.info(String.format("Finished downloading %s", mod.getModName()));
			}).start();
		}
		log.trace(String.format("Finished downloading %s mods.", MOD_LIST.size()));
	}

	public static final void updateMods() {
		log.trace(String.format("Updating %s mods...", Reference.updateTotal));
		int updateCount = 1;
		for (val mod : MOD_LIST) {
			log.info(String.format(Reference.UPDATING_MOD_X_OF_Y, mod.getModName(), updateCount++,
					Reference.updateTotal));
			new Thread(() -> {
				MOD_HANDLERS.get(mod.getClass()).updateMod(mod);
				Reference.updateCount++;
				log.info(String.format("Finished updating %s", mod.getModName()));
			}).start();
		}
		log.trace(String.format("Finished updating %s mods.", Reference.updateTotal));
	}

	public static void updateManifest() {
		log.info("Updating Manifest File...");
		try {
			val file = new FileWriter(Reference.manifestFile);
			file.write(buildManifestJson());
			file.flush();
			file.close();
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static String buildManifestJson() {
		JSONObject manifest = new JSONObject();

		if (processedCurseMods) {
			manifest.put("curseFiles", new JSONArray());
		}

		if (processedThirdPartyMods) {
			manifest.put("thirdParty", new JSONArray());
		}

		for (val mod : MOD_LIST) {
			if (mod instanceof CurseMod && manifest.containsKey("curseFiles")) {
				((JSONArray) manifest.get("curseFiles")).add(CurseModMapper.map((CurseMod) mod));
			} else if (mod instanceof ThirdPartyMod && manifest.containsKey("thirdParty")) {
				((JSONArray) manifest.get("thirdParty")).add(ThirdPartyMapper.map((ThirdPartyMod) mod));
			}
		}

		return manifest.toString();
	}
}

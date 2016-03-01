package com.nincraft.modpackdownloader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nincraft.modpackdownloader.container.CurseMod;
import com.nincraft.modpackdownloader.container.ModContainer;
import com.nincraft.modpackdownloader.container.ThirdPartyMod;
import com.nincraft.modpackdownloader.handler.CurseModHandler;
import com.nincraft.modpackdownloader.handler.ThirdPartyModHandler;
import com.nincraft.modpackdownloader.manager.DownloadManager;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ModPackDownloader {

	private static final List<ModContainer> MOD_LIST = new ArrayList<ModContainer>();

	public static void main(final String[] args) throws InterruptedException {
		if (args.length < 2) {
			log.error("Arguments required: manifest file location, mod download location");
			return;
		} else {
			processArguments(args);
		}

		init();

		setupRepo();

		processMods();
	}

	private static void processArguments(final String[] args) {
		Reference.manifestFile = args[0];
		Reference.modFolder = args[1];

		if (args.length > 2) {
			for (val arg : args) {
				processArgument(arg);
			}
		}
	}

	private static void init() {
		log.trace("Registering various mod type handlers...");
		Reference.MOD_HANDLERS.put(CurseMod.class, new CurseModHandler());
		Reference.MOD_HANDLERS.put(ThirdPartyMod.class, new ThirdPartyModHandler());
		log.trace("Finished registering various mod type handlers.");
	}

	private static void processArgument(final String arg) {
		log.trace("Processing given arguments...");
		if (arg.equals("-forceDownload")) {
			Reference.forceDownload = true;
			log.debug("Downloads are now being forced.");
		} else if (arg.equals("-updateMods")) {
			Reference.updateMods = true;
			log.debug("mods will be updated instead of downloaded.");
		} else if (arg.startsWith("-mcVersion")) {
			Reference.mcVersion = arg.substring(arg.lastIndexOf("=") + 1);
			log.debug(String.format("Minecraft Version set to: %s", Reference.mcVersion));
		} else if (arg.startsWith("-releaseType")) {
			Reference.releaseType = arg.substring(arg.lastIndexOf("=") + 1);
			log.debug(String.format("Checking against mod release type: %s", Reference.releaseType));
		} else if (arg.equals("-generateUrlTxt")) {
			Reference.generateUrlTxt = true;
			log.debug("Mod URL Text files will now be generated.");
		}
		log.trace("Finished processing given arguments.");
	}

	private static void setupRepo() {
		log.trace("Setting up local repository...");
		Reference.userhome = System.getProperty("user.home");
		log.debug(String.format("User Home System Property detected as: %s", Reference.userhome));

		Reference.os = System.getProperty("os.name");
		log.debug(String.format("Operating System detected as: %s", Reference.os));

		if (Reference.os.startsWith("Windows")) {
			Reference.userhome += Reference.WINDOWS_FOLDER;
		} else if (Reference.os.startsWith("Mac")) {
			Reference.userhome += Reference.MAC_FOLDER;
		} else {
			Reference.userhome += Reference.OTHER_FOLDER;
		}
		log.debug(String.format("User Home Folder set to: %s", Reference.userhome));

		FileSystemHelper.createFolder(Reference.userhome);
		log.trace("Finished setting up local repository.");
	}

	private static void processMods() throws InterruptedException {
		log.trace("Processing Mods...");
		buildModList();

		if (Reference.updateMods) {
			log.info(String.format("Updating mods with parameters: %s, %s, %s", Reference.manifestFile,
					Reference.mcVersion, Reference.releaseType));
			// UpdateManager.updateMods(MOD_LIST);
			ModUpdater.updateCurseMods(MOD_LIST, Reference.mcVersion, Reference.releaseType);

			while (!checkUpdateFinished()) {
				Thread.sleep(1);
			}
			log.info("Finished updating mods.");
		} else {
			log.info(String.format("Downloading mods with parameters: %s, %s", Reference.manifestFile,
					Reference.modFolder));
			DownloadManager.downloadMods(MOD_LIST);

			while (!checkDownloadFinished()) {
				Thread.sleep(1);
			}
			log.info("Finished downloading mods.");
		}
		log.trace("Finished Processing Mods.");
	}

	private static void buildModList() {
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
				MOD_LIST.add(mod);
				log.debug(String.format("Curse Mod '%s' found.", mod.getModName()));
			}
		}

		val thirdPartyMods = getThirdPartyModList(jsonLists);
		if (thirdPartyMods != null) {
			for (val thirdPartyMod : thirdPartyMods) {
				val mod = new ThirdPartyMod((JSONObject) thirdPartyMod);
				MOD_LIST.add(mod);
				log.debug(String.format("Third Party Mod '%s' found.", mod.getModName()));
			}
		}

		Reference.updateCount = Reference.downloadTotal = MOD_LIST.size();
		log.debug(String.format("A total of %s mods will be %s.", Reference.downloadTotal,
				Reference.updateMods ? "updated" : "downloaded"));

		MOD_LIST.sort((mod1, mod2) -> mod1.getModName().compareToIgnoreCase(mod2.getModName()));
		log.trace("Finished Building Mod List.");
	}

	private static JSONArray getCurseModList(final JSONObject jsonList) {
		return (JSONArray) (jsonList.containsKey("curseFiles") ? jsonList.get("curseFiles") : jsonList.get("files"));
	}

	private static JSONArray getThirdPartyModList(final JSONObject jsonLists) {
		return (JSONArray) jsonLists.get("thirdParty");
	}

	private static boolean checkUpdateFinished() {
		return Reference.updateCount == Reference.updateTotal;
	}

	private static boolean checkDownloadFinished() {
		return Reference.downloadCount == Reference.downloadTotal;
	}
}

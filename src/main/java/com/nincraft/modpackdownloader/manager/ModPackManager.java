package com.nincraft.modpackdownloader.manager;

import com.google.gson.Gson;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.handler.CurseFileHandler;
import com.nincraft.modpackdownloader.status.DownloadStatus;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Log4j2
public class ModPackManager {

	private static Gson gson = new Gson();

	public static boolean updateModPack() {
		boolean returnStatus = true;
		log.trace("Updating Curse modpack");
		String modPackIdName = "";
		try {
			modPackIdName = FileUtils.readFileToString(new File("modpackid"));
			log.info(String.format("Found modpackid file with id %s", modPackIdName));
		} catch (IOException e) {
			log.error("Could not find modpackid file", e);
			return false;
		}
		String modPackId = modPackIdName.substring(0, modPackIdName.indexOf('-'));
		if (!NumberUtils.isNumber(modPackId)) {
			log.error(String.format("Unable to find a valid project ID, found %s", modPackId));
			return false;
		}
		String modPackName = modPackIdName.substring(modPackIdName.indexOf('-') + 1);
		CurseFile modPack = new CurseFile(modPackId, modPackName);
		modPack.initModpack();
		Arguments.mcVersion = "*";
		CurseFileHandler.updateCurseFile(modPack);
		if (!modPack.getFileName().contains(".zip")) {
			modPack.setFileName(modPack.getFileName() + ".zip");
		}
		Arguments.modFolder = ".";
		if (DownloadStatus.SKIPPED.equals(DownloadHelper.downloadFile(modPack, false))) {
			log.info(String.format("No new updates found for %s", modPack.getName()));
			returnStatus = false;
		}
		Arguments.modFolder = "mods";
		File modsFolder = new File(Arguments.modFolder);
		File backupModsFolder = new File("backupmods");
		try {
			FileUtils.moveDirectory(modsFolder, backupModsFolder);
		} catch (IOException e) {
			log.error("Could not backup mod folder", e);
			return false;
		}
		try {
			ZipFile modPackZip = new ZipFile(modPack.getFileName());
			modPackZip.extractAll(".");
		} catch (ZipException e) {
			log.error("Could not unzip modpack", e);
			try {
				FileUtils.moveDirectory(backupModsFolder, modsFolder);
			} catch (IOException e1) {
				log.error("Could not restore backup mods folder", e1);
				return false;
			}
			return false;
		}
		try {
			FileUtils.deleteDirectory(backupModsFolder);
		} catch (IOException e) {
			log.error("Unable to delete backup mods folder", e);
		}
		return returnStatus;
	}

	public static void handlePostDownload() {
		try {
			File overrides = new File("overrides");
			FileUtils.copyDirectory(overrides, new File("."), true);
			FileUtils.deleteDirectory(overrides);
		} catch (IOException e) {
			log.error(e);
		}

	}

	public static void checkPastForgeVersion() {
		if (new File(".").getAbsolutePath().contains("MultiMC")) {
			JSONObject currentJson = null;
			JSONObject multiMCJson = null;
			try {
				currentJson = (JSONObject) new JSONParser().parse(new FileReader(Arguments.manifestFile));
				multiMCJson = (JSONObject) new JSONParser().parse(new FileReader("../patches/net.minecraftforge.json"));
			} catch (IOException | ParseException e) {
				log.error(e);
				return;
			}
			Manifest currentManifestFile = gson.fromJson(currentJson.toString(), Manifest.class);
			String manifestForge = currentManifestFile.getForgeVersion();
			String multiMCForge = (String) multiMCJson.get("version");
			if (!manifestForge.contains(multiMCForge)) {
				log.error(String.format("Current MultiMC Forge version is not the same as the current downloaded pack, please update this instance's Forge to %s", manifestForge));
				System.exit(1);
			}
		}
	}
}

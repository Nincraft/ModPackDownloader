package com.nincraft.modpackdownloader.manager;

import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.handler.CurseFileHandler;
import com.nincraft.modpackdownloader.status.DownloadStatus;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Log4j2
public class ModPackManager {
	public static boolean updateModPack() {
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
		String modPackName = modPackIdName.substring(modPackIdName.indexOf('-') + 1);
		CurseFile modPack = new CurseFile(modPackId, modPackName);
		modPack.initModpack();
		Reference.mcVersion = "*";
		CurseFileHandler.updateCurseFile(modPack);
		if (!modPack.getFileName().contains(".zip")) {
			modPack.setFileName(modPack.getFileName() + ".zip");
		}
		Reference.modFolder = ".";
		if (DownloadStatus.SKIPPED.equals(DownloadHelper.downloadFile(modPack, false))) {
			log.info(String.format("No new updates found for %s", modPack.getName()));
			return false;
		}
		Reference.modFolder = "mods";
		try {
			ZipFile modPackZip = new ZipFile(modPack.getFileName());
			modPackZip.extractAll(".");
		} catch (ZipException e) {
			log.error("Could not unzip modpack", e);
			return false;
		}

		return true;
	}

	public static void handleOverrides() {
		try {
			File overrides = new File("overrides");
			FileUtils.copyDirectory(overrides, new File("."), true);
			FileUtils.deleteDirectory(overrides);
		} catch (IOException e) {
			log.error(e);
		}
	}
}

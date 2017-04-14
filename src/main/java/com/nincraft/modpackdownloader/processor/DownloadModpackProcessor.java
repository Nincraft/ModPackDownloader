package com.nincraft.modpackdownloader.processor;

import com.google.gson.Gson;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.CurseModpackFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.handler.CurseFileHandler;
import com.nincraft.modpackdownloader.status.DownloadStatus;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import lombok.extern.log4j.Log4j2;
import lombok.val;
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
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

@Log4j2
public class DownloadModpackProcessor extends AbstractProcessor {

	private Gson gson;

	private CurseFileHandler curseFileHandler;

	public DownloadModpackProcessor(Arguments arguments, DownloadHelper downloadHelper) {
		super(arguments, downloadHelper);
	}

	private boolean backupModsFolder(File modsFolder, File backupModsFolder) {
		if (modsFolder.exists()) {
			try {
				FileUtils.moveDirectory(modsFolder, backupModsFolder);
			} catch (IOException e) {
				log.error("Could not backup mod folder", e);
				return true;
			}
		}
		return false;
	}

	private boolean checkSuccessfulDownloadStatus(DownloadStatus downloadStatus) {
		return DownloadStatus.SUCCESS_DOWNLOAD.equals(downloadStatus) || DownloadStatus.SUCCESS_CACHE.equals(downloadStatus);
	}

	@Override
	protected void init(final Map<File, Manifest> manifestMap) {
		gson = new Gson();
		curseFileHandler = new CurseFileHandler(arguments, downloadHelper);
	}

	@Override
	public void process() throws InterruptedException {
		init(manifestMap);

		for (val manifestEntry : manifestMap.entrySet()) {
			preprocess(manifestEntry);

			if (process(manifestEntry)) {
				postProcess(manifestEntry);
			}
		}
	}

	@Override
	boolean preprocess(final Entry<File, Manifest> manifest) {
		return true;
	}

	@Override
	boolean process(final Entry<File, Manifest> manifest) throws InterruptedException {
		boolean returnStatus;
		log.trace("Updating Curse modpack");
		String modPackIdName;
		try {
			modPackIdName = FileUtils.readFileToString(new File("modpackid"), Charset.defaultCharset());
			log.info("Found modpackid file with id {}", modPackIdName);
		} catch (IOException e) {
			log.error("Could not find modpackid file", e);
			return false;
		}
		String modPackId = modPackIdName.substring(0, modPackIdName.indexOf('-'));
		if (!NumberUtils.isCreatable(modPackId)) {
			log.error("Unable to find a valid project ID, found {}", modPackId);
			return false;
		}
		String modPackName = modPackIdName.substring(modPackIdName.indexOf('-') + 1);
		CurseModpackFile modPack = new CurseModpackFile(modPackId, modPackName);
		modPack.init();
		arguments.setMcVersion("*");
		curseFileHandler.updateCurseFile(modPack);
		arguments.setModFolder(".");
		modPack.setDownloadUrl(modPack.getCurseForgeDownloadUrl());
		getDownloadUrl(modPack, true);
		if (!modPack.getFileName().contains(".zip")) {
			modPack.setFileName(modPack.getName() + ".zip");
		}
		DownloadStatus downloadStatus = downloadHelper.downloadFile(modPack, false);

		if (DownloadStatus.FAILURE.equals(downloadStatus)) {
			log.warn("Failed to download {}. Attempting redownload with FTB URL", modPack.getName());
			modPack.setDownloadUrl(modPack.getCurseForgeDownloadUrl(false));
			getDownloadUrl(modPack, false);
			downloadStatus = downloadHelper.downloadFile(modPack, false);
		}

		if (DownloadStatus.SKIPPED.equals(downloadStatus)) {
			log.info("No new updates found for {}", modPack.getName());
		}

		returnStatus = checkSuccessfulDownloadStatus(downloadStatus);
		arguments.setModFolder("mods");
		File modsFolder = new File(arguments.getModFolder());
		File backupModsFolder = new File("backupmods");
		if (backupModsFolder(modsFolder, backupModsFolder)) {
			return false;
		}
		try {
			ZipFile modPackZip = new ZipFile(modPack.getFileName());
			modPackZip.extractAll(".");
			log.info("Successfully unzipped modpack");
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

	@Override
	boolean postProcess(final Entry<File, Manifest> manifest) {
		checkPastForgeVersion();
		handlePostDownload();
		return true;
	}

	private void getDownloadUrl(CurseFile modPack, boolean isCurseForge) {
		try {
			curseFileHandler.getCurseForgeDownloadLocation(modPack, isCurseForge);
		} catch (IOException e) {
			log.error("Failed to get download location for {}", modPack.getName(), e);
		}
	}

	private void handlePostDownload() {
		try {
			File overrides = new File("overrides");
			FileUtils.copyDirectory(overrides, new File("."), true);
			FileUtils.deleteDirectory(overrides);
		} catch (IOException e) {
			log.error(e);
		}

	}

	private void checkPastForgeVersion() {
		if (new File(".").getAbsolutePath().contains("MultiMC")) {
			JSONObject currentJson;
			JSONObject multiMCJson;
			try {
				currentJson = (JSONObject) new JSONParser().parse(new FileReader(arguments.getManifests().get(0)));
				multiMCJson = (JSONObject) new JSONParser().parse(new FileReader("../patches/net.minecraftforge.json"));
			} catch (IOException | ParseException e) {
				log.error(e);
				return;
			}
			Manifest currentManifestFile = gson.fromJson(currentJson.toString(), Manifest.class);
			String manifestForge = currentManifestFile.getForgeVersion();
			String multiMCForge = (String) multiMCJson.get("version");
			if (!manifestForge.contains(multiMCForge)) {
				log.error(
						"Current MultiMC Forge version is not the same as the current downloaded pack, please update this instance's Forge to {}",
						manifestForge);
				System.exit(1);
			}
		}
	}
}

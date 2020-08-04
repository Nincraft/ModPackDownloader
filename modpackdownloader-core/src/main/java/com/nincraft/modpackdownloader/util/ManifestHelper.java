package com.nincraft.modpackdownloader.util;

import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.container.Minecraft;
import com.nincraft.modpackdownloader.container.ModLoader;
import lombok.val;

import java.util.List;

public class ManifestHelper {

	/**
	 * Cleanup Mod Lists after an update
	 * @param manifest
	 */
	public static void cleanupModLists(Manifest manifest) {

		val minecraft = manifest.getMinecraft();
		if (minecraft != null) {
			val modLoaders = minecraft.getModLoaders();
			if (modLoaders != null && modLoaders.isEmpty()) {
				minecraft.setModLoaders(null);
			}
		}

		if (manifest.getCurseFiles().isEmpty()) {
			manifest.setCurseFiles(null);
		}
		if (manifest.getThirdParty().isEmpty()) {
			manifest.setThirdParty(null);
		}

		// Always Clean up Batch Add
		manifest.setBatchAddCurse(null);
	}

	/**
	 * Clean up Merged Manifest for Curseforge deployment
	 *
	 * @param manifest
	 */
	public static void cleanupManifest(Manifest manifest) {
		val minecraft = manifest.getMinecraft();

		if (minecraft != null) {
			cleanupMinecraft(minecraft);
		}

		val curseFiles = manifest.getCurseFiles();
		if (curseFiles != null) {
			cleanupCurseFiles(curseFiles);
		}

		manifest.setThirdParty(null);
		manifest.setBatchAddCurse(null);
	}

	private static void cleanupMinecraft(Minecraft minecraft) {
		val modLoaders = minecraft.getModLoaders();

		if (modLoaders != null) {
			if (modLoaders.isEmpty()) {
				minecraft.setModLoaders(null);
			} else {
				cleanupModLoaders(modLoaders);
			}
		}
	}

	private static void cleanupModLoaders(List<ModLoader> modLoaders) {
		for (val modLoader : modLoaders) {
			cleanupModLoader(modLoader);
		}
	}

	private static void cleanupModLoader(ModLoader modLoader) {
		modLoader.setName(null);
		modLoader.setFolder(null);
		modLoader.setRelease(null);
		modLoader.setDownloadInstaller(null);
		modLoader.setDownloadUniversal(null);
		modLoader.setRenameInstaller(null);
		modLoader.setRenameUniversal(null);
	}

	private static void cleanupCurseFiles(List<CurseFile> curseFiles) {
		for (val curseFile : curseFiles) {
			cleanupCurseFile(curseFile);
		}
	}

	private static void cleanupCurseFile(CurseFile curseFile) {
		curseFile.setName(null);
		curseFile.setSkipDownload(null);
		curseFile.setSkipUpdate(null);
	}
}

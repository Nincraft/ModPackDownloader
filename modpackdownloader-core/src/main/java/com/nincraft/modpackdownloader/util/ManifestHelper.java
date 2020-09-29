package com.nincraft.modpackdownloader.util;

import com.nincraft.modpackdownloader.container.*;
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

		if (manifest.getCurseAddons().isEmpty()) {
			manifest.setCurseAddons(null);
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

		val curseAddons = manifest.getCurseAddons();
		if (curseAddons != null) {
			cleanupCurseAddons(curseAddons);
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

	private static void cleanupCurseAddons(List<CurseAddon> curseAddons) {
		for (val curseAddon : curseAddons) {
			cleanupCurseAddon(curseAddon);
		}
	}

	private static void cleanupCurseAddon(CurseAddon curseAddon) {
	    curseAddon.setAddonID(null);
        cleanupCurseFile(curseAddon.getInstalledFile());
    }

    private static void cleanupCurseFile(CurseFile curseFile) {
        curseFile.setName(null);
        curseFile.setSkipDownload(null);
        curseFile.setSkipUpdate(null);
    }
}

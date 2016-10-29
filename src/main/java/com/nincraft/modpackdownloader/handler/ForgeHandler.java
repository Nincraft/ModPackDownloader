package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.ModLoader;
import com.nincraft.modpackdownloader.status.DownloadStatus;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;

@Log4j2
public class ForgeHandler {
	public static void downloadForge(String minecraftVersion, List<ModLoader> modLoaders) {
		if (CollectionUtils.isEmpty(modLoaders) || Strings.isNullOrEmpty(minecraftVersion)) {
			log.debug("No Forge or Minecraft version found in manifest, skipping");
			return;
		}

		for (ModLoader modLoader : modLoaders) {
			if (BooleanUtils.isTrue(modLoader.getDownloadInstaller())) {
				log.info(String.format("Downloading Forge installer version %s", modLoader.getId()));
				downloadForgeFile(minecraftVersion, modLoader, true);
			}
			if (BooleanUtils.isTrue(modLoader.getDownloadUniversal())) {
				log.info(String.format("Downloading Forge universal version %s", modLoader.getId()));
				downloadForgeFile(minecraftVersion, modLoader, false);
			}
		}
	}

	private static void downloadForgeFile(String minecraftVersion, ModLoader modLoader, boolean downloadInstaller) {
		downloadForgeFile(minecraftVersion, modLoader, downloadInstaller, true);
	}

	private static void downloadForgeFile(String minecraftVersion, ModLoader modLoader, boolean downloadInstaller, boolean alternateDownloadUrl) {
		modLoader.setRename(modLoader.getRename(downloadInstaller));
		String forgeFileName = "forge-" + minecraftVersion + "-" + modLoader.getForgeId();
		String forgeURL = Reference.forgeURL + minecraftVersion + "-" + modLoader.getForgeId();
		if (alternateDownloadUrl) {
			forgeFileName += "-" + minecraftVersion;
			forgeURL += "-" + minecraftVersion;
		}

		forgeFileName += downloadInstaller ? Reference.forgeInstaller : Reference.forgeUniversal;
		forgeURL += "/" + forgeFileName;

		modLoader.setDownloadUrl(forgeURL);
		modLoader.setFileName(forgeFileName);
		if (DownloadStatus.FAILURE.equals(DownloadHelper.downloadFile(modLoader)) && alternateDownloadUrl) {
			log.warn("Attempting alternate Forge download URL");
			downloadForgeFile(minecraftVersion, modLoader, downloadInstaller, false);
		}
	}

	public static List<ModLoader> updateForge(String minecraftVersion, List<ModLoader> modLoaders) {
		if (!Arguments.updateForge) {
			log.trace("Updating Forge disabled");
			return modLoaders;
		}

		for (ModLoader modLoader : modLoaders) {
			JSONObject fileListJson = null;
			if (modLoader.getRelease() == null) {
				log.warn("No Forge release type set for update, defaulting to recommended");
				modLoader.setRelease("recommended");
			}
			try {
				fileListJson = (JSONObject) (URLHelper.getJsonFromUrl(Reference.forgeUpdateURL)).get("promos");
				String updatedForgeVersion = (String) fileListJson.get(minecraftVersion + "-" + modLoader.getRelease());
				String manifestForgeVersion = modLoader.getId().substring(modLoader.getId().indexOf('-') + 1);

				if (compareVersions(manifestForgeVersion, updatedForgeVersion) < 0) {
					log.info(String.format("Newer version of Forge found, updating to %s", updatedForgeVersion));
					modLoader.setId("forge-" + updatedForgeVersion);
				}

			} catch (IOException | ParseException e) {
				log.error("Failed to update Forge", e);
			}
		}

		return modLoaders;
	}

	private static int compareVersions(String manifestForgeVersion, String updatedForgeVersion) {
		String[] manArr = manifestForgeVersion.split("\\.");
		String[] updateArr = updatedForgeVersion.split("\\.");

		int i = 0;

		while (i < manArr.length || i < updateArr.length) {
			if (Integer.parseInt(manArr[i]) < Integer.parseInt(updateArr[i])) {
				return -1;
			} else if (Integer.parseInt(manArr[i]) > Integer.parseInt(updateArr[i])) {
				return 1;
			}
			i++;
		}

		return 0;
	}
}

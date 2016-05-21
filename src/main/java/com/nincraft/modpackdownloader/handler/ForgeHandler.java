package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.ModLoader;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

@Log4j2
public class ForgeHandler {
	public static void downloadForge(String minecraftVersion, List<ModLoader> modLoaders) {
		if (CollectionUtils.isEmpty(modLoaders) || Strings.isNullOrEmpty(minecraftVersion)) {
			log.debug("No Forge or Minecraft version found in manifest, skipping");
			return;
		}

		for (ModLoader modLoader : modLoaders) {
			String forgeVersion = modLoader.getId();
			String folder = modLoader.getFolder();
			String forgeId = forgeVersion.substring(forgeVersion.indexOf("-") + 1);

			log.info(String.format("Downloading Forge version %s", forgeVersion));

			if (BooleanUtils.isTrue(modLoader.getDownloadInstaller())) {
				downloadForgeFile(minecraftVersion, modLoader, folder, forgeId, true);
			}
			if (BooleanUtils.isTrue(modLoader.getDownloadUniversal())) {
				downloadForgeFile(minecraftVersion, modLoader, folder, forgeId, false);
			}
		}
	}

	private static void downloadForgeFile(String minecraftVersion, ModLoader modLoader, String folder, String forgeId, boolean downloadInstaller) {
		downloadForgeFile(minecraftVersion, modLoader, folder, forgeId, downloadInstaller, false);
	}

	private static void downloadForgeFile(String minecraftVersion, ModLoader modLoader, String folder, String forgeId, boolean downloadInstaller, boolean alternateDownloadUrl) {
		String forgeFileName = "forge-" + minecraftVersion + "-" + forgeId;
		String forgeURL = Reference.forgeURL + minecraftVersion + "-" + forgeId;
		if (alternateDownloadUrl) {
			forgeFileName += "-" + minecraftVersion;
			forgeURL += "-" + minecraftVersion;
		}

		forgeFileName += downloadInstaller ? Reference.forgeInstaller : Reference.forgeUniversal;
		forgeURL += "/" + forgeFileName;

		if (!FileSystemHelper.isInLocalRepo("forge", forgeFileName) || Reference.forceDownload) {
			File downloadedFile;
			if (modLoader.getRename(downloadInstaller) != null) {
				downloadedFile = FileSystemHelper.getDownloadedFile(modLoader.getRename(downloadInstaller), folder);
			} else {
				downloadedFile = FileSystemHelper.getDownloadedFile(forgeFileName, folder);
			}

			try {
				FileUtils.copyURLToFile(new URL(forgeURL), downloadedFile);
			} catch (final IOException e) {
				log.error(String.format("Could not download %s.", forgeFileName), e.getMessage());
				if (!alternateDownloadUrl) {
					log.warn("Attempting alternate Forge download URL");
					downloadForgeFile(minecraftVersion, modLoader, folder, forgeId, downloadInstaller, true);
				}
				return;
			}
			FileSystemHelper.copyToLocalRepo("forge", downloadedFile, forgeFileName);
		} else {
			FileSystemHelper.copyFromLocalRepo("forge", forgeFileName, folder, modLoader.getRename(downloadInstaller));
		}
		log.info(String.format("Completed downloading Forge version %s", forgeFileName));
	}

	public static List<ModLoader> updateForge(String minecraftVersion, List<ModLoader> modLoaders) {
		if (!Reference.updateForge) {
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
				fileListJson = (JSONObject) ((JSONObject) new JSONParser().parse(new BufferedReader(new InputStreamReader(new URL(Reference.forgeUpdateURL).openStream())))).get("promos");
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

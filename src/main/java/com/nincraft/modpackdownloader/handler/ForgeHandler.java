package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.ModLoader;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Log4j2
public class ForgeHandler {
	public static void downloadForge(String minecraftVersion, List<ModLoader> modLoaders) {
		if (modLoaders == null || Strings.isNullOrEmpty(minecraftVersion)) {
			log.debug("No Forge or Minecraft version found in manifest, skipping");
			return;
		}

		ModLoader modLoader = modLoaders.get(0);
		String forgeVersion = modLoader.getId();
		String folder = modLoader.getFolder();
		String forgeId = forgeVersion.substring(forgeVersion.indexOf("-") + 1);

		log.info(String.format("Downloading Forge version %s", forgeVersion));

		if (modLoader.getDownloadInstaller()) {
			downloadForgeFile(minecraftVersion, modLoader, folder, forgeId, Reference.forgeInstaller);
		}
		if (modLoader.getDownloadUniversal()) {
			downloadForgeFile(minecraftVersion, modLoader, folder, forgeId, Reference.forgeUniversal);
		}
	}

	private static void downloadForgeFile(String minecraftVersion, ModLoader modLoader, String folder, String forgeId, String fileType) {
		String forgeFileName = "forge-" + minecraftVersion + "-" + forgeId;
		String forgeURL = Reference.forgeURL + minecraftVersion + "-" + forgeId;
		if (!minecraftVersion.startsWith("1.8")) {
			forgeFileName += "-" + minecraftVersion;
			forgeURL += "-" + minecraftVersion;
		}
		forgeFileName += fileType;
		forgeURL += "/" + forgeFileName;

		if (!FileSystemHelper.isInLocalRepo("forge", forgeFileName) || Reference.forceDownload) {
			File downloadedFile;
			if (modLoader.getRename() != null) {
				downloadedFile = FileSystemHelper.getDownloadedFile(modLoader.getRename(), folder);
			} else {
				downloadedFile = FileSystemHelper.getDownloadedFile(forgeFileName, folder);
			}

			try {
				FileUtils.copyURLToFile(new URL(forgeURL), downloadedFile);
			} catch (final IOException e) {
				log.error(String.format("Could not download %s.", forgeFileName), e.getMessage());
				return;
			}
			FileSystemHelper.copyToLocalRepo("forge", downloadedFile);
		} else {
			FileSystemHelper.copyFromLocalRepo("forge", forgeFileName, Reference.modFolder);
		}
		log.info(String.format("Completed downloading Forge version %s", forgeFileName));
	}
}

package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Log4j2
public class ForgeHandler {
	public static void downloadForgeInstaller(String minecraftVersion, String forgeVersion) {
		if (Strings.isNullOrEmpty(forgeVersion) || Strings.isNullOrEmpty(minecraftVersion)) {
			log.debug("No Forge or Minecraft version found in manifest, skipping");
			return;
		}

		log.info(String.format("Downloading Forge version %s", forgeVersion));

		String forgeId = forgeVersion.substring(forgeVersion.indexOf("-") + 1);
		String forgeFileName = "forge-" + minecraftVersion + "-" + forgeId;
		String forgeURL = Reference.forgeURL + minecraftVersion + "-" + forgeId;
		if (!minecraftVersion.startsWith("1.8")) {
			forgeFileName += "-" + minecraftVersion;
			forgeURL += "-" + minecraftVersion;
		}
		forgeFileName += "-installer.jar";
		forgeURL += "/" + forgeFileName;

		if (!FileSystemHelper.isInLocalRepo("forge", forgeFileName) || Reference.forceDownload) {
			val downloadedFile = FileSystemHelper.getDownloadedFile(forgeFileName);
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

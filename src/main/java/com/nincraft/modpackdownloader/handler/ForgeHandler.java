package com.nincraft.modpackdownloader.handler;

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
		log.info(String.format("Downloading Forge version %s", forgeVersion));

		String forgeId = forgeVersion.substring(forgeVersion.indexOf("-") + 1);
		String forgeURL = Reference.forgeURL + minecraftVersion + "-" + forgeId + "-" + minecraftVersion +
				"/forge-" + minecraftVersion + "-" + forgeId + "-" + minecraftVersion + "-installer.jar";
		String forgeFileName = "forge-" + minecraftVersion + "-" + forgeId + "-" + minecraftVersion + "-installer.jar";
		File forge = FileSystemHelper.getDownloadedFile(forgeFileName);

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

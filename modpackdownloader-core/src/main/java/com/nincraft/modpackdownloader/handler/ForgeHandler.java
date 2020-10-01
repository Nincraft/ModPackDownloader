package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.ModLoader;
import com.nincraft.modpackdownloader.status.DownloadStatus;
import com.nincraft.modpackdownloader.util.*;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;

@Log4j2
public class ForgeHandler {

	private Reference reference = Reference.getInstance();
	private Arguments arguments;
	private DownloadHelper downloadHelper;

	public ForgeHandler(Arguments arguments, DownloadHelper downloadHelper) {
		this.arguments = arguments;
		this.downloadHelper = downloadHelper;
	}

	public void downloadForge(String minecraftVersion, List<ModLoader> modLoaders) {
		if (CollectionUtils.isEmpty(modLoaders) /*|| Strings.isNullOrEmpty(minecraftVersion)*/) {
			log.debug("No Forge or Minecraft version found in manifest, skipping");
			return;
		}

		for (val modLoader : modLoaders) {
			if (BooleanUtils.isTrue(modLoader.getDownloadInstaller())) {
				log.debug("Downloading Forge installer version {}", modLoader.getId());
				downloadForgeFile(/*minecraftVersion,*/ modLoader, true);
			}
			if (BooleanUtils.isTrue(modLoader.getDownloadUniversal())) {
				log.debug("Downloading Forge universal version {}", modLoader.getId());
				downloadForgeFile(/*minecraftVersion,*/ modLoader, false);
			}
		}
	}

	/*private void downloadForgeFile(ModLoader modLoader, boolean downloadInstaller) {
		downloadForgeFile(modLoader, downloadInstaller, true);
	}*/

	private void downloadForgeFile(/*String minecraftVersion,*/ ModLoader modLoader, boolean downloadInstaller/*, boolean alternateDownloadUrl*/) {
		modLoader.setRename(modLoader.getRename(downloadInstaller));
		/*String forgeFileName = "forge-" + minecraftVersion + "-" + modLoader.getForgeId();
		String forgeURL = reference.getForgeUrl() + minecraftVersion + "-" + modLoader.getForgeId();
		if (alternateDownloadUrl) {
			forgeFileName += "-" + minecraftVersion;
			forgeURL += "-" + minecraftVersion;
		}

		forgeFileName += downloadInstaller ? reference.getForgeInstaller() : reference.getForgeUniversal();
		forgeURL += "/" + forgeFileName;

		modLoader.setDownloadUrl(forgeURL);
		modLoader.setFileName(forgeFileName);*/
		if (DownloadStatus.FAILURE.equals(downloadHelper.downloadFile(modLoader)) /*&& alternateDownloadUrl*/) {
		    log.error("Failed to download Forge");
			/*log.warn("Attempting alternate Forge download URL");
			downloadForgeFile(modLoader, downloadInstaller, false);*/
		}
	}

	public List<ModLoader> updateForge(String minecraftVersion, List<ModLoader> modLoaders) {
		if (!arguments.isUpdateForge()) {
			log.trace("Updating Forge disabled");
			return modLoaders;
		}

		for (ModLoader modLoader : modLoaders) {
			JSONObject fileListJson;
			if (modLoader.getRelease() == null) {
				log.warn("No Forge release type set for update, defaulting to recommended");
				modLoader.setRelease("recommended");
			}
			try {
				fileListJson = (JSONObject) (URLHelper.getJsonFromUrl(reference.getForgeUpdateUrl())).get("promos");
				String updatedForgeVersion = (String) fileListJson.get(minecraftVersion + "-" + modLoader.getRelease());
				String manifestForgeVersion = modLoader.getId().substring(modLoader.getId().indexOf('-') + 1);

				if (VersionHelper.compareVersions(manifestForgeVersion, updatedForgeVersion) < 0) {
					log.debug("Newer version of Forge found, updating to {}", updatedForgeVersion);
					modLoader.setId("forge-" + updatedForgeVersion);
				}

			} catch (IOException | ParseException e) {
				log.error("Failed to update Forge", e);
			}
		}

		return modLoaders;
	}

}

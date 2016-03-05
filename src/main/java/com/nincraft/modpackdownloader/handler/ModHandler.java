package com.nincraft.modpackdownloader.handler;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ModHandler {

	public abstract void downloadMod(final Mod mod);

	public abstract void updateMod(final Mod mod);

	protected static void downloadFile(final Mod mod, final boolean useUserAgent) {
		val decodedFileName = URLHelper.decodeSpaces(mod.getFileName());

		if (!FileSystemHelper.isInLocalRepo(mod.getModName(), decodedFileName) || Reference.forceDownload) {
			val downloadedFile = FileSystemHelper.getDownloadedFile(mod, decodedFileName);
			try {
				FileUtils.copyURLToFile(new URL(mod.getDownloadUrl()), downloadedFile);
			} catch (final IOException e) {
				log.error(String.format("Could not download %s.", mod.getFileName()), e.getMessage());
				Reference.downloadCount++;
				return;
			}

			if (Reference.generateUrlTxt) {
				generateUrlTxt(downloadedFile, mod);
			}

			FileSystemHelper.copyToLocalRepo(mod.getModName(), downloadedFile);
		} else {
			FileSystemHelper.copyFromLocalRepo(mod.getModName(), decodedFileName, Reference.modFolder);
		}
	}

	protected static void generateUrlTxt(final File downloadedFile, final Mod mod) {
		if (Reference.modFolder != null) {
			new File(Reference.modFolder + File.separator + downloadedFile.getName() + ".url.txt");
		} else {
			new File(downloadedFile.getName() + "url.txt");
		}
	}
}

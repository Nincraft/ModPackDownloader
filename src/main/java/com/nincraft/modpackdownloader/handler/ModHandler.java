package com.nincraft.modpackdownloader.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

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
			try {
				ReadableByteChannel rbc;

				if (useUserAgent) {
					val conn = (HttpURLConnection) new URL(mod.getDownloadUrl()).openConnection();
					conn.addRequestProperty("User-Agent", "Mozilla/4.0");
					rbc = Channels.newChannel(conn.getInputStream());
				} else {
					rbc = Channels.newChannel(new URL(mod.getDownloadUrl()).openStream());
				}

				val downloadedFile = FileSystemHelper.getDownloadedFile(mod, decodedFileName);

				val fos = new FileOutputStream(downloadedFile);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();

				if (Reference.generateUrlTxt) {
					generateUrlTxt(downloadedFile, mod);
				}

				FileSystemHelper.copyToLocalRepo(mod.getModName(), downloadedFile);
			} catch (final IOException e) {
				if (!useUserAgent) {
					log.warn(String.format("Error getting %s. Attempting to redownload using alternate method.",
							mod.getFileName()));
					downloadFile(mod, true);
				} else {
					log.error(String.format("Could not download %s.", mod.getFileName()), e.getMessage());
				}
			}
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

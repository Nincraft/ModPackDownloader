package com.nincraft.modpackdownloader.handler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseMod;
import com.nincraft.modpackdownloader.container.ModContainer;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CurseModHandler extends ModHandler {

	@Override
	public void downloadMod(final ModContainer mod) {
		downloadCurseMod((CurseMod) mod);
	}

	@Override
	public void updateMod(final ModContainer mod) {
		updateCurseMod((CurseMod) mod);
	}

	private void downloadCurseMod(final CurseMod mod) {
		val modName = mod.getModName();

		try {
			val fileName = !Strings.isNullOrEmpty(mod.getRename()) ? mod.getRename()
					: getCurseForgeDownloadLocation(mod.getDownloadUrl(), modName, modName);
			mod.setFileName(fileName);

			downloadFile(mod, false);
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
	}

	private void updateCurseMod(final CurseMod mod) {
		// TODO Auto-generated method stub

	}

	private static String getCurseForgeDownloadLocation(final String url, final String projectName,
			final String downloadLocation) throws IOException, MalformedURLException {
		String encodedDownloadLocation = URLHelper.encodeSpaces(downloadLocation);

		if (encodedDownloadLocation.indexOf(Reference.JAR_FILE_EXT) == -1) {
			val newUrl = url + Reference.COOKIE_TEST_1;

			HttpURLConnection conn = (HttpURLConnection) new URL(newUrl).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			String actualURL = conn.getURL().toString();
			int retryCount = 0;

			while (conn.getResponseCode() != 200 || actualURL.indexOf(Reference.JAR_FILE_EXT) == -1) {
				val headerLocation = conn.getHeaderField("Location");
				if (headerLocation != null) {
					actualURL = headerLocation;
				} else {
					actualURL = conn.getURL().toString();
				}

				if (retryCount > Reference.RETRY_COUNTER) {
					break;
				}

				conn = (HttpURLConnection) new URL(newUrl).openConnection();
				retryCount++;
			}

			if (actualURL.substring(actualURL.lastIndexOf(Reference.URL_DELIMITER) + 1)
					.indexOf(Reference.JAR_FILE_EXT) != -1) {
				encodedDownloadLocation = actualURL.substring(actualURL.lastIndexOf(Reference.URL_DELIMITER) + 1);
			} else {
				encodedDownloadLocation = projectName + Reference.JAR_FILE_EXT;
			}
		}

		return URLHelper.decodeSpaces(encodedDownloadLocation);
	}
}

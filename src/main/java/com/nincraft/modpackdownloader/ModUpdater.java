package com.nincraft.modpackdownloader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseMod;
import com.nincraft.modpackdownloader.container.ModContainer;
import com.nincraft.modpackdownloader.mapper.CurseModMapper;
import com.nincraft.modpackdownloader.util.Reference;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ModUpdater {
	public static void updateCurseMods(final List<ModContainer> modList, final String mcVersion,
			final String releaseType) {
		log.trace("Updating Curse Mods...");
		val curseMods = new JSONArray();

		checkForUpdates(modList, mcVersion, releaseType, curseMods);

		updateManifest(curseMods);
		log.trace("Finished Updating Curse Mods.");
	}

	@SuppressWarnings("unchecked")
	private static void updateManifest(final JSONArray curseMods) {
		log.info("Updating Manifest File...");
		try {
			val jsonLists = (JSONObject) new JSONParser().parse(new FileReader(Reference.manifestFile));
			log.debug(jsonLists);
			jsonLists.remove("curseFiles");
			log.debug(jsonLists);
			jsonLists.put("curseFiles", curseMods);
			log.debug(jsonLists);

			val file = new FileWriter(Reference.manifestFile);
			file.write(jsonLists.toJSONString());
			file.flush();
			file.close();
		} catch (final IOException | ParseException e) {
			log.error(e.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked" })
	private static void checkForUpdates(final List<ModContainer> modList, final String mcVersion,
			final String releaseType, final org.json.simple.JSONArray curseMods) {
		log.info(String.format("Checking for updates from %s mods.", modList.size()));
		for (val mod : modList) {
			if (!(mod instanceof CurseMod)) {
				log.debug(String.format("Mod '%s' is not a Curse Mod, and will be skipped.", mod.getModName()));
				Reference.updateCount++;
				continue;
			}

			val curseMod = (CurseMod) mod;

			JSONObject fileListJson = null;
			try {
				val conn = (HttpURLConnection) new URL(curseMod.getProjectUrl()).openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.connect();

				val location = conn.getHeaderField("Location");
				curseMod.setProjectName(location.split("/")[2]);
				fileListJson = (JSONObject) getCurseProjectJson(curseMod.getProjectId(), curseMod.getProjectName(),
						new JSONParser()).get("files");

				if (fileListJson == null) {
					log.error(String.format("No file list found for %s, and will be skipped.",
							curseMod.getProjectName()));
					Reference.updateCount++;
					continue;
				}
			} catch (IOException | ParseException e) {
				log.error(e.getMessage());
				continue;
			}

			val newMod = getLatestVersion(mcVersion, releaseType, curseMod, fileListJson);
			log.debug(newMod);
			if (curseMod.getFileId().compareTo(newMod.getFileId()) < 0) {
				log.info(String.format("Update found for %s.  Most recent version is %s.  Old version was %s.",
						curseMod.getProjectName(), newMod.getVersion(), curseMod.getVersion()));
				curseMod.setFileId(newMod.getFileId());
				curseMod.setVersion(newMod.getVersion());
			}

			if (Strings.isNullOrEmpty(curseMod.getModName())) {
				curseMod.setModName(curseMod.getProjectName());
			}

			val json = CurseModMapper.map(curseMod);
			log.debug(json);
			curseMods.add(json);
			Reference.updateCount++;
		}
		log.info("Finished checking for updates.");
	}

	private static CurseMod getLatestVersion(final String mcVersion, final String releaseType, final CurseMod curseMod,
			final JSONObject fileListJson) {
		log.trace("Getting most recent available file...");
		CurseMod newMod = null;
		try {
			newMod = curseMod.clone();
		} catch (CloneNotSupportedException e) {
			log.warn("Couldn't clone existing mod reference, creating new one instead.");
			newMod = new CurseMod();
		}

		for (val newFileJson : fileListJson.values()) {
			val newModJson = (JSONObject) newFileJson;
			val date = parseDate((String) newModJson.get("created_at"));

			Date latestDate = date;
			if (!latestDate.after(date) && equalOrLessThan((String) newModJson.get("type"), releaseType)
					&& newModJson.get("version").equals(mcVersion)) {
				newMod.setFileId((Long) newModJson.get("id"));
				newMod.setVersion((String) newModJson.get("name"));
				latestDate = date;
			}

			if (curseMod.getFileId().equals(newMod.getFileId())) {
				log.debug("Ensuring the current version is set on the mod.");
				curseMod.setVersion(newMod.getVersion());
			}
		}
		log.trace("Finished getting most recent available file.");
		return newMod;
	}

	private static boolean equalOrLessThan(final String modRelease, final String releaseType) {
		return releaseType.equals(modRelease) || "beta".equals(releaseType) && "release".equals(modRelease);
	}

	private static JSONObject getCurseProjectJson(final Long projectID, final String projectName,
			final JSONParser projectParser) throws ParseException, IOException {
		log.trace("Getting CurseForge Widget JSON...");
		try {
			String urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, projectName);
			log.debug(urlStr);
			return (JSONObject) projectParser
					.parse(new BufferedReader(new InputStreamReader(new URL(urlStr).openStream())));
		} catch (final FileNotFoundException e) {
			String urlStr = String.format(Reference.CURSEFORGE_WIDGET_JSON_URL, projectID + "-" + projectName);
			log.debug(urlStr);
			return (JSONObject) projectParser
					.parse(new BufferedReader(new InputStreamReader(new URL(urlStr).openStream())));
		} finally {
			log.trace("Finished Getting CurseForge Widget JSON.");
		}
	}

	private static Date parseDate(final String date) {
		for (val parse : Reference.DATE_FORMATS) {
			try {
				return new SimpleDateFormat(parse).parse(date);
			} catch (final java.text.ParseException e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}

}

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
		try {
			log.info(String.format("Checking for updates from %s mods.", modList.size()));
			for (val mod : modList) {
				if (!(mod instanceof CurseMod)) {
					log.debug(String.format("Mod '%s' is not a Curse Mod, and will be skipped.", mod.getModName()));
					continue;
				}

				val curseMod = (CurseMod) mod;

				final HttpURLConnection conn = (HttpURLConnection) new URL(curseMod.getProjectUrl()).openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.connect();

				val location = conn.getHeaderField("Location");
				val projectName = location.split("/")[2];
				val fileListJson = (JSONObject) getCurseProjectJson(curseMod.getProjectId(), projectName,
						new JSONParser()).get("files");

				if (fileListJson == null) {
					log.error(String.format("No file list found for %s.", projectName));
					continue;
				}

				Date lastDate = null;
				Long mostRecent = curseMod.getFileId();
				String mostRecentFile = null;
				String currentFile = null;

				log.info("Getting most recent available file...");
				for (val newFileJson : fileListJson.values()) {
					val newMod = (JSONObject) newFileJson;
					val date = parseDate((String) newMod.get("created_at"));

					if (lastDate == null) {
						lastDate = date;
					}

					if (lastDate.before(date) && equalOrLessThan((String) newMod.get("type"), releaseType)
							&& newMod.get("version").equals(mcVersion)) {
						mostRecent = (Long) newMod.get("id");
						mostRecentFile = (String) newMod.get("name");
						lastDate = date;
					}

					if (curseMod.getFileId().equals(newMod.get("id"))) {
						currentFile = (String) newMod.get("name");
					}
				}
				log.info("Finished getting most recent available file.");

				if (!mostRecent.equals(curseMod.getFileId())) {
					log.info(String.format("Update found for %s.  Most recent version is %s.  Old version was %s.",
							projectName, mostRecentFile, currentFile));
					curseMod.setFileId(mostRecent);
				}

				if (Strings.isNullOrEmpty(curseMod.getModName())) {
					curseMod.setModName(projectName);
				}

				val json = CurseModMapper.map(curseMod);
				log.debug(json);
				curseMods.add(json);
			}
			log.info("Finished checking for updates.");

			log.info("Updating Manifest File...");
			FileWriter file = null;
			try {
				val jsonLists = (JSONObject) new JSONParser().parse(new FileReader(Reference.manifestFile));
				log.debug(jsonLists);
				jsonLists.remove("curseFiles");
				log.debug(jsonLists);
				jsonLists.put("curseFiles", curseMods);
				log.debug(jsonLists);

				file = new FileWriter(Reference.manifestFile);
				file.write(jsonLists.toJSONString());
			} finally {
				file.flush();
				file.close();
			}
		} catch (final IOException | ParseException e) {
			log.error(e.getMessage());
		}
		log.trace("Finished Updating Curse Mods.");
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

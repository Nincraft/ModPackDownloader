package com.nincraft.modpackdownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseMod;
import com.nincraft.modpackdownloader.container.ModContainer;
import com.nincraft.modpackdownloader.container.ThirdPartyMod;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ModPackDownloader {

	private static int DOWNLOAD_TOTAL = 0;
	private static AtomicInteger DOWNLOAD_COUNT = new AtomicInteger(0);

	private static final List<ModContainer> MOD_LIST = new ArrayList<ModContainer>();

	public static void main(final String[] args) throws InterruptedException {
		if (args.length < 2) {
			log.error("Arguments required: manifest file location, mod download location");
			return;
		} else {
			processArguments(args);
		}

		setupRepo();

		processMods();
	}

	private static void processArguments(final String[] args) {
		Reference.manifestFile = args[0];
		Reference.modFolder = args[1];

		if (args.length > 2) {
			for (val arg : args) {
				processArgument(arg);
			}
		}
	}

	private static void processArgument(final String arg) {
		log.trace("Processing given arguments...");
		if (arg.equals("-forceDownload")) {
			Reference.forceDownload = true;
			log.debug("Downloads are now being forced.");
		} else if (arg.equals("-updateMods")) {
			Reference.updateMods = true;
			log.debug("mods will be updated instead of downloaded.");
		} else if (arg.startsWith("-mcVersion")) {
			Reference.mcVersion = arg.substring(arg.lastIndexOf("=") + 1);
			log.debug(String.format("Minecraft Version set to: %s", Reference.mcVersion));
		} else if (arg.startsWith("-releaseType")) {
			Reference.releaseType = arg.substring(arg.lastIndexOf("=") + 1);
			log.debug(String.format("Checking against mod release type: %s", Reference.releaseType));
		} else if (arg.equals("-generateUrlTxt")) {
			Reference.generateUrlTxt = true;
			log.debug("Mod URL Text files will now be generated.");
		}
		log.trace("Finished processing given arguments.");
	}

	private static void setupRepo() {
		log.trace("Setting up local repository...");
		Reference.userhome = System.getProperty("user.home");
		log.debug(String.format("User Home System Property detected as: %s", Reference.userhome));

		Reference.os = System.getProperty("os.name");
		log.debug(String.format("Operating System detected as: %s", Reference.os));

		if (Reference.os.startsWith("Windows")) {
			Reference.userhome += Reference.WINDOWS_FOLDER;
		} else if (Reference.os.startsWith("Mac")) {
			Reference.userhome += Reference.MAC_FOLDER;
		} else {
			Reference.userhome += Reference.OTHER_FOLDER;
		}
		log.debug(String.format("User Home Folder set to: %s", Reference.userhome));

		createFolder(Reference.userhome);
		log.trace("Finished setting up local repository.");
	}

	private static void processMods() throws InterruptedException {
		log.trace("Processing Mods...");
		buildModList();

		if (Reference.updateMods) {
			log.info(String.format("Updating mods with parameters: %s, %s, %s", Reference.manifestFile,
					Reference.mcVersion, Reference.releaseType));
			ModUpdater.updateCurseMods(MOD_LIST, Reference.mcVersion, Reference.releaseType);
			log.info("Finished updating mods.");
		} else {
			log.info(String.format("Starting download with parameters: %s, %s", Reference.manifestFile,
					Reference.modFolder));
			downloadMods(MOD_LIST, Reference.modFolder);

			while (!checkFinished()) {
				Thread.sleep(1);
			}
			log.info("Finished downloading mods.");
		}
		log.trace("Finished Processing Mods.");
	}

	private static void buildModList() {
		log.trace("Building Mod List...");
		JSONObject jsonLists = null;
		try {
			jsonLists = (JSONObject) new JSONParser().parse(new FileReader(Reference.manifestFile));
		} catch (IOException | ParseException e) {
			log.error(e.getMessage());
			return;
		}

		val curseMods = getCurseModList(jsonLists);
		if (curseMods != null) {
			for (val curseMod : curseMods) {
				val mod = new CurseMod((JSONObject) curseMod);
				MOD_LIST.add(mod);
				log.debug(String.format("Curse Mod '%s' found.", mod.getModName()));
			}
		}

		val thirdPartyMods = getThirdPartyModList(jsonLists);
		if (thirdPartyMods != null) {
			for (val thirdPartyMod : thirdPartyMods) {
				val mod = new ThirdPartyMod((JSONObject) thirdPartyMod);
				MOD_LIST.add(mod);
				log.debug(String.format("Third Party Mod '%s' found.", mod.getModName()));
			}
		}

		DOWNLOAD_TOTAL = MOD_LIST.size();
		log.debug(String.format("A total of %s mods will be downloaded.", DOWNLOAD_TOTAL));

		MOD_LIST.sort((mod1, mod2) -> mod1.getModName().compareToIgnoreCase(mod2.getModName()));
		log.trace("Finished Building Mod List.");
	}

	private static JSONArray getCurseModList(final JSONObject jsonList) {
		return (JSONArray) (jsonList.containsKey("curseFiles") ? jsonList.get("curseFiles") : jsonList.get("files"));
	}

	private static JSONArray getThirdPartyModList(final JSONObject jsonLists) {
		return (JSONArray) jsonLists.get("thirdParty");
	}

	private static boolean checkFinished() {
		return DOWNLOAD_COUNT.get() == DOWNLOAD_TOTAL;
	}

	private static void downloadMods(final List<ModContainer> modList, final String modFolder) {
		log.trace(String.format("Starting download of %s mods...", DOWNLOAD_TOTAL));
		int curseCount = 1;
		int thirdPartyCount = 1;

		for (val mod : modList) {
			mod.getModName();

			if (mod instanceof CurseMod) {
				downloadCurseMod((CurseMod) mod, curseCount++, modFolder);
			} else if (mod instanceof ThirdPartyMod) {
				downloadThirdPartyMod((ThirdPartyMod) mod, thirdPartyCount++, modFolder);
			}
		}
		log.trace(String.format("Finished downloading %s mods.", DOWNLOAD_TOTAL));
	}

	private static void downloadCurseMod(final CurseMod mod, final int downloadCount, final String modFolder) {
		new Thread(() -> {
			String modName = mod.getModName();

			log.info(String.format(Reference.DOWNLOADING_MOD_X_OF_Y, modName, DOWNLOAD_COUNT.incrementAndGet(), DOWNLOAD_TOTAL));
			downloadCurseForgeFile(mod);
			log.info(String.format("Finished downloading %s", modName));
		}).start();
	}

	private static void downloadThirdPartyMod(final ThirdPartyMod mod, final int downloadCount,
			final String modFolder) {
		new Thread(() -> {
			String modName = mod.getModName();

			log.info(String.format(Reference.DOWNLOADING_MOD_X_OF_Y, modName, DOWNLOAD_COUNT.incrementAndGet(), DOWNLOAD_TOTAL));
			downloadFile(mod, false);
			log.info(String.format("Finished downloading %s", modName));
		}).start();
	}

	private static void createFolder(final String folder) {
		if (folder != null) {
			final File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

	private static void downloadCurseForgeFile(final CurseMod mod) {
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

	private static void downloadFile(final ModContainer mod, final boolean useUserAgent) {
		val decodedFileName = URLHelper.decodeSpaces(mod.getFileName());

		if (!isInLocalRepo(mod.getModName(), decodedFileName) || Reference.forceDownload) {
			try {
				ReadableByteChannel rbc;

				if (useUserAgent) {
					val conn = (HttpURLConnection) new URL(mod.getDownloadUrl()).openConnection();
					conn.addRequestProperty("User-Agent", "Mozilla/4.0");
					rbc = Channels.newChannel(conn.getInputStream());
				} else {
					rbc = Channels.newChannel(new URL(mod.getDownloadUrl()).openStream());
				}

				val downloadedFile = getDownloadedFile(mod, decodedFileName);

				val fos = new FileOutputStream(downloadedFile);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();

				if (Reference.generateUrlTxt) {
					generateUrlTxt(downloadedFile, mod);
				}

				copyToLocalRepo(mod.getModName(), downloadedFile);
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
			copyFromLocalRepo(mod.getModName(), decodedFileName, Reference.modFolder);
		}
	}

	private static File getDownloadedFile(final ModContainer mod, final String fileName) {
		if (Reference.modFolder != null) {
			createFolder(Reference.modFolder);
			return new File(Reference.modFolder + File.separator + fileName);
		} else {
			return new File(fileName);
		}
	}

	private static void generateUrlTxt(final File downloadedFile, final ModContainer mod) {
		if (Reference.modFolder != null) {
			new File(Reference.modFolder + File.separator + downloadedFile.getName() + ".url.txt");
		} else {
			new File(downloadedFile.getName() + "url.txt");
		}
	}

	private static void copyToLocalRepo(final String projectName, final File downloadedFile) {
		val newProjectName = projectName != null ? projectName : "thirdParty";

		try {
			val localRepoFolder = new File(Reference.userhome + newProjectName);
			FileUtils.copyFileToDirectory(downloadedFile, localRepoFolder);
		} catch (final IOException e) {
			log.error(String.format("Could not copy %s to local repo.", newProjectName), e);
		}
	}

	private static void copyFromLocalRepo(final String projectName, final String fileName, final String folder) {
		val newProjectName = projectName != null ? projectName : "thirdParty";

		try {
			final File localRepoMod = new File(Reference.userhome + newProjectName + File.separator + fileName);
			FileUtils.copyFileToDirectory(localRepoMod, new File(folder));
		} catch (final IOException e) {
			log.error(String.format("Could not copy %s from local repo.", newProjectName), e);
		}
	}

	private static boolean isInLocalRepo(final String projectName, final String fileName) {
		val newProjectName = projectName != null ? projectName : "thirdParty";

		return new File(Reference.userhome + newProjectName + File.separator + fileName).exists();
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

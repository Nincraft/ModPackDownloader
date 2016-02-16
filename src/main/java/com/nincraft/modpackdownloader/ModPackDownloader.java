package com.nincraft.modpackdownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nincraft.modpackdownloader.util.Reference;

public class ModPackDownloader {

	private static int DOWNLOAD_COUNT = 1;
	static Logger logger = LogManager.getRootLogger();

	public static void main(String[] args) {
		if (args.length < 2) {
			logger.error("Arguments required: manifest file location, mod download location");
			return;
		} else {
			processArguments(args);
		}

		setupRepo();
		if (Reference.updateMods) {
			ModUpdater.updateCurseMods(Reference.manifestFile, Reference.mcVersion, Reference.releaseType);
		} else {
			logger.info("Starting download with parameters: " + Reference.manifestFile + ", " + Reference.modFolder);
			downloadCurseMods(Reference.manifestFile, Reference.modFolder);
			downloadThirdPartyMods(Reference.manifestFile, Reference.modFolder);
			logger.info("Finished downloading mods");
		}
	}

	private static void processArguments(String[] args) {
		Reference.manifestFile = args[0];
		Reference.modFolder = args[1];
		if (args.length > 2) {
			for (String arg : args) {
				if (arg.equals("-forceDownload")) {
					Reference.forceDownload = true;
				} else if (arg.equals("-updateMods")) {
					Reference.updateMods = true;
				} else if (arg.startsWith("-mcVersion")) {
					Reference.mcVersion = arg.substring(arg.lastIndexOf("-mcVersion") + "-mcVersion".length());
				} else if (arg.startsWith("-releaseType")) {
					Reference.releaseType = arg.substring(arg.indexOf("-releaseType") + "-releaseType".length());
				}
			}
		}
	}

	private static void setupRepo() {
		Reference.userhome = System.getProperty("user.home");
		Reference.os = System.getProperty("os.name");
		if (Reference.os.startsWith("Windows")) {
			Reference.userhome += Reference.windowsFolder;
		} else if (Reference.os.startsWith("Mac")) {
			Reference.userhome += Reference.macFolder;
		} else {
			Reference.userhome += Reference.otherFolder;
		}
		createFolder(Reference.userhome);
	}

	private static void downloadFromGithubSource(String manifestFile, String modFolder) {
		try {
			String URL = "https://github.com/TPPIDev/Modpack-Tweaks/archive/824ef29f76bab126f4299724ab4f9e658b340639.zip";
			downloadFile(URL, "github", "Modpack-Tweaks.zip", "Modpack Tweaks", false);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void downloadThirdPartyMods(String manifestFile, String modFolder) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject jsons = (JSONObject) parser.parse(new FileReader(manifestFile));
			JSONArray urlList = (JSONArray) jsons.get("thirdParty");
			if (urlList != null) {
				Iterator iterator = urlList.iterator();
				logger.info("Starting download of " + urlList.size() + " 3rd party mods");
				DOWNLOAD_COUNT = 1;
				while (iterator.hasNext()) {
					JSONObject urlJson = (JSONObject) iterator.next();
					String url = (String) urlJson.get("url");
					String projectName = (String) urlJson.get("name");
					String fileName;
					if (urlJson.get("rename") == null) {
						fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".jar") + 4);
					} else {
						fileName = (String) urlJson.get("rename");
					}
					logger.info("Downloading " + fileName + ". Mod " + DOWNLOAD_COUNT + " of " + urlList.size());
					downloadFile(url, modFolder, fileName, projectName, false);
					DOWNLOAD_COUNT++;
				}
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
	}

	private static void downloadCurseMods(String manifestFile, String modFolder) {
		JSONParser parser = new JSONParser();
		try {
			Long projectID;
			Long fileID;
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(manifestFile));
			JSONArray fileList = (JSONArray) jsonObject.get("curseFiles");
			if (fileList == null) {
				fileList = (JSONArray) jsonObject.get("files");
			}
			if (fileList != null) {
				logger.info("Starting download of " + fileList.size() + " mods from Curse");
				Iterator iterator = fileList.iterator();
				DOWNLOAD_COUNT = 1;
				while (iterator.hasNext()) {
					JSONObject modJson = (JSONObject) iterator.next();
					projectID = (Long) modJson.get("projectID");
					fileID = (Long) modJson.get("fileID");
					String url = Reference.CURSEFORGE_BASE_URL + projectID + Reference.COOKIE_TEST_1;
					HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
					con.setInstanceFollowRedirects(false);
					con.connect();
					String location = con.getHeaderField("Location");
					String projectName = location.split("/")[2];
					logger.info("Downloading " + projectName + ". Mod " + DOWNLOAD_COUNT + " of " + fileList.size());
					downloadCurseForgeFile(createCurseDownloadUrl(projectName, fileID), modFolder, projectName,
							modJson);
					DOWNLOAD_COUNT++;
				}
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}
	}

	private static String createCurseDownloadUrl(String projectName, Long fileID) {
		return Reference.CURSEFORGE_BASE_URL + projectName + "/files/" + fileID + "/download";
	}

	private static void createFolder(String folder) {
		if (folder != null) {
			File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

	private static void downloadCurseForgeFile(String url, String folder, String projectName, JSONObject modJson) {
		String fileName = projectName;
		try {
			if (modJson.get("rename") == null) {
				fileName = getCurseForgeDownloadLocation(url, projectName, fileName);
			} else {
				fileName = (String) modJson.get("rename");
			}
			downloadFile(url, folder, fileName, projectName, false);
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error("Could not find: " + fileName, e);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static void downloadFile(String url, String folder, String fileName, String projectName,
			boolean useUserAgent) throws MalformedURLException, FileNotFoundException {
		try {
			fileName = fileName.replace("%20", " ");
			if (!isInLocalRepo(projectName, fileName) || Reference.forceDownload) {
				ReadableByteChannel rbc;
				if (useUserAgent) {
					HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
					con.addRequestProperty("User-Agent", "Mozilla/4.0");
					rbc = Channels.newChannel(con.getInputStream());

				} else {
					URL fileThing = new URL(url);
					rbc = Channels.newChannel(fileThing.openStream());
				}
				FileOutputStream fos;
				File downloadedFile;
				if (folder != null) {
					createFolder(folder);
					downloadedFile = new File(folder + File.separator + fileName);
					fos = new FileOutputStream(downloadedFile);
				} else {
					downloadedFile = new File(fileName);
					fos = new FileOutputStream(downloadedFile);
				}
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				copyToLocalRepo(projectName, downloadedFile);
			} else {
				copyFromLocalRepo(projectName, fileName, folder);
			}
		} catch (IOException e) {
			logger.warn("Error getting " + fileName + ". Attempting redownload with alternate method.");
			downloadFile(url, folder, fileName, projectName, true);
		}
	}

	private static void copyToLocalRepo(String projectName, File downloadedFile) {
		try {
			if (projectName == null) {
				projectName = "thirdParty";
			}
			File localRepoFolder = new File(Reference.userhome + projectName);
			FileUtils.copyFileToDirectory(downloadedFile, localRepoFolder);
		} catch (IOException e) {
			logger.error("Could not copy " + projectName + " to local repo", e);
		}
	}

	private static void copyFromLocalRepo(String projectName, String fileName, String folder) {
		try {
			if (projectName == null) {
				projectName = "thirdParty";
			}
			File localRepoMod = new File(Reference.userhome + projectName + File.separator + fileName);
			FileUtils.copyFileToDirectory(localRepoMod, new File(folder));
		} catch (IOException e) {
			logger.error("Could not copy " + projectName + " from local repo", e);
		}
	}

	private static boolean isInLocalRepo(String projectName, String fileName) {
		if (projectName == null) {
			projectName = "thirdParty";
		}
		File localCheck = new File(Reference.userhome + projectName + File.separator + fileName);
		return localCheck.exists();
	}

	private static String getCurseForgeDownloadLocation(String url, String projectName, String downloadLocation)
			throws IOException, MalformedURLException {
		final String jarext = ".jar";
		if (downloadLocation.indexOf(jarext) == -1) {
			url = url + Reference.COOKIE_TEST_1;
			HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
			con.setInstanceFollowRedirects(false);
			con.connect();
			String actualURL = con.getURL().toString();
			int retryCount = 0;
			String headerLocation;
			while (con.getResponseCode() != 200 || actualURL.indexOf(jarext) == -1) {
				headerLocation = con.getHeaderField("Location");
				if (headerLocation != null) {
					actualURL = headerLocation;
				} else {
					actualURL = con.getURL().toString();
				}
				if (retryCount > Reference.RETRY_COUNTER) {
					break;
				}
				con = (HttpURLConnection) (new URL(url).openConnection());
				retryCount++;
			}

			if (actualURL.substring(actualURL.lastIndexOf('/') + 1).indexOf(jarext) != -1)
				downloadLocation = actualURL.substring(actualURL.lastIndexOf('/') + 1);
			else
				downloadLocation = projectName + jarext;
		}

		return downloadLocation.replace("%20", " ");
	}

}

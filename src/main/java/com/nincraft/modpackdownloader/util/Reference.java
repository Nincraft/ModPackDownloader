package com.nincraft.modpackdownloader.util;

public class Reference {
	public static final String CURSEFORGE_BASE_URL = "https://minecraft.curseforge.com/projects/";
	public static final String CURSEFORGE_WIDGET_JSON_MOD = "mc-mods";
	public static final String CURSEFORGE_WIDGET_JSON_MODPACK = "modpacks";
	public static final String CURSEFORGE_WIDGET_JSON_URL = "http://widget.mcf.li/%s/minecraft/%s.json";
	public static final String COOKIE_TEST_1 = "?cookieTest=1";
	public static final String DOWNLOADING_MOD_X_OF_Y = "Downloading %s. Mod %s of %s.";
	public static final String UPDATING_MOD_X_OF_Y = "Updating %s. Mod %s of %s.";
	public static final String WINDOWS_FOLDER = "\\.modpackdownloader\\";
	public static final String MAC_FOLDER = "/Library/Application Support/modpackdownloader/";
	public static final String OTHER_FOLDER = "/.modpackdownloader/";
	public static final String JAR_FILE_EXT = ".jar";
	public static final int RETRY_COUNTER = 5;
	public static final char URL_DELIMITER = '/';
	public static final String DEFAULT_MANIFEST_FILE = "manifest.json";
	public static String userhome;
	public static String os;
	public static int downloadCount = 0;
	public static int downloadTotal = 0;
	public static int updateCount = 0;
	public static int updateTotal = 0;
	public static String updateAppURL = "http://play.nincraft.com:8080/job/Mod%20Pack%20Downloader/lastStableBuild/artifact/target/classes/latest.json";
	public static String forgeURL = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/";
	public static String forgeInstaller = "-installer.jar";
	public static String forgeUniversal = "-universal.jar";
	public static String forgeUpdateURL = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json";
	public static String javaContentType = "application/java-archive";
}

package com.nincraft.modpackdownloader.util;

public class Reference {
	public static final String CURSEFORGE_BASE_URL = "http://minecraft.curseforge.com/projects/";
	public static final String CURSEFORGE_WIDGET_JSON_URL = "http://widget.mcf.li/mc-mods/minecraft/%s.json";
	public static final String COOKIE_TEST_1 = "?cookieTest=1";
	public static final String DOWNLOADING_MOD_X_OF_Y = "Downloading %s. Mod %s of %s.";
	public static final String UPDATING_MOD_X_OF_Y = "Updating %s. Mod %s of %s.";
	public static final String WINDOWS_FOLDER = "\\.modpackdownloader\\";
	public static final String MAC_FOLDER = "/Library/Application Support/modpackdownloader/";
	public static final String OTHER_FOLDER = "/.modpackdownloader/";
	public static final String JAR_FILE_EXT = ".jar";
	public static final String[] DATE_FORMATS = { "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss'Z'",
			"yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
			"yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", "MM/dd/yyyy'T'HH:mm:ss.SSSZ",
			"MM/dd/yyyy'T'HH:mm:ss.SSS", "MM/dd/yyyy'T'HH:mm:ssZ", "MM/dd/yyyy'T'HH:mm:ss", "yyyy:MM:dd HH:mm:ss", };
	public static final int RETRY_COUNTER = 5;
	public static final char URL_DELIMITER = '/';
	public static String userhome;
	public static String os;
	public static String manifestFile;
	public static String modFolder;
	public static boolean forceDownload = false;
	public static boolean updateMods;
	public static String mcVersion;
	public static String releaseType;
	public static boolean generateUrlTxt;
	public static int downloadCount = 0;
	public static int downloadTotal = 0;
	public static int updateCount = 0;
	public static int updateTotal = 0;
}

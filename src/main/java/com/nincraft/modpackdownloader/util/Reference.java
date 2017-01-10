package com.nincraft.modpackdownloader.util;

import lombok.Data;
import lombok.Getter;

@Data
public class Reference {
	@Getter
	private static final Reference instance = new Reference();
	public static int downloadCount = 0;
	public static int downloadTotal = 0;
	public static int updateCount = 0;
	public static int updateTotal = 0;
	private String curseforgeBaseUrl = "https://minecraft.curseforge.com/projects/";
	private String ftbBaseUrl = "https://www.feed-the-beast.com/projects/";
	private String curseforgeWidgetJsonMod = "mc-mods";
	private String curseforgeWidgetJsonModpack = "modpacks";
	private String curseforgeWidgetJsonUrl = "http://widget.mcf.li/%s/minecraft/%s.json";
	private String cookieTest1 = "?cookieTest=1";
	private String downloadingModXOfY = "Downloading %s. Mod %s of %s.";
	private String updatingModXOfY = "Updating %s. Mod %s of %s.";
	private String windowsFolder = "\\.modpackdownloader\\";
	private String macFolder = "/Library/Application Support/modpackdownloader/";
	private String otherFolder = "/.modpackdownloader/";
	private String jarFileExt = ".jar";
	private String zipFileExt = ".zip";
	private int retryCounter = 5;
	private char urlDelimiter = '/';
	private String defaultManifestFile = "manifest.json";
	private String userhome;
	private String os;
	private String updateAppUrl = "http://play.nincraft.com:8080/job/Mod%20Pack%20Downloader/lastSuccessfulBuild/artifact/target/classes/latest.json";
	private String forgeUrl = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/";
	private String forgeInstaller = "-installer.jar";
	private String forgeUniversal = "-universal.jar";
	private String forgeUpdateUrl = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json";
	private String javaContentType = "application/java-archive";

	private Reference() {
		//no-op
	}
}

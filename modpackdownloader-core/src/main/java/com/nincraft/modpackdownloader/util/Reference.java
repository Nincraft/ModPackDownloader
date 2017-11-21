package com.nincraft.modpackdownloader.util;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor(access = PRIVATE)
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
	private String curseforgeWidgetJsonUrl = "https://api.cfwidget.com/%s/minecraft/%s.json";
	private String cookieTest1 = "?cookieTest=1";
	private String downloadingModXOfY = "Downloading {}. Mod {} of {}.";
	private String updatingModXOfY = "Updating {}. Mod {} of {}.";
	private String windowsFolder = "\\.modpackdownloader\\";
	private String macFolder = "/Library/Application Support/modpackdownloader/";
	private String otherFolder = "/.modpackdownloader/";
	private String jarFileExt = ".jar";
	private String zipFileExt = ".zip";
	private int retryCounter = 5;
	private char urlDelimiter = '/';
	private String defaultManifestFile = "manifest.json";
	private String userhome;
	private String updateAppUrl = "http://play.nincraft.com:8080/job/Mod%20Pack%20Downloader/lastSuccessfulBuild/artifact/modpackdownloader-core/target/classes/latest.json";
	private String forgeUrl = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/";
	private String forgeInstaller = "-installer.jar";
	private String forgeUniversal = "-universal.jar";
	private String forgeUpdateUrl = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json";
	private String javaContentType = "application/java-archive";
}

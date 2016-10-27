package com.nincraft.modpackdownloader.util;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;
import java.util.List;

public class Arguments {
	@Parameter(names = { "-modFolder", "-folder", "-mods" })
	public static String modFolder;
	@Parameter(names = { "-mcVersion" })
	public static String mcVersion;
	@Parameter(names = { "-backupVersion", "-backupVersions" })
	public static List<String> backupVersions;
	@Parameter(names = { "-releaseType" })
	public static String releaseType;

	@Parameter(names = { "-forceDownload" })
	public static boolean forceDownload = false;
	@Parameter(names = { "-generateUrlTxt" })
	public static boolean generateUrlTxt;
	@Parameter(names = { "-updateForge" })
	public static boolean updateForge;
	@Parameter(names = { "-updateCurseModPack" })
	public static boolean updateCurseModPack;
	@Parameter(names = { "-updateApp" })
	public static boolean updateApp;
	@Parameter(names = { "-clearCache" })
	public static boolean clearCache;

	@Parameter(names = { "-manifest", "-manifests" }, listConverter = FileConverter.class)
	public static List<File> manifests;
	@Parameter(names = {"-downloadMods"})
	public static boolean downloadMods;
	@Parameter(names = { "-updateMods" })
	public static boolean updateMods;
	@Parameter(names = { "-mergeManifests" })
	public static boolean mergeManifests;
}

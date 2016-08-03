package com.nincraft.modpackdownloader.util;

import com.beust.jcommander.Parameter;

import java.util.List;

public class Arguments {
	@Parameter(names = {"-manifest"})
	public static String manifestFile;
	@Parameter(names = {"-modFolder", "-folder", "-mods"})
	public static String modFolder;
	@Parameter(names = {"-forceDownload"})
	public static boolean forceDownload = false;
	@Parameter(names = {"-updateMods"})
	public static boolean updateMods;
	@Parameter(names = {"-mcVersion"})
	public static String mcVersion;
	@Parameter(names = {"-backupVersions"})
	public static List<String> backupVersions;
	@Parameter(names = {"-releaseType"})
	public static String releaseType;
	@Parameter(names = {"-generateUrlTxt"})
	public static boolean generateUrlTxt;
	@Parameter(names = {"-updateForge"})
	public static boolean updateForge;
	@Parameter(names = {"-updateCurseModPack"})
	public static boolean updateCurseModPack;
	@Parameter(names = {"-updateApp"})
	public static boolean updateApp;
	@Parameter(names = {"-clearCache"})
	public static boolean clearCache;
}

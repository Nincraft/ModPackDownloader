package com.nincraft.modpackdownloader.util;

import com.beust.jcommander.Parameter;

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
}

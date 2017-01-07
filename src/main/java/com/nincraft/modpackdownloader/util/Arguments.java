package com.nincraft.modpackdownloader.util;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.nincraft.modpackdownloader.validation.ExistingFile;
import com.nincraft.modpackdownloader.validation.ReleaseType;

import java.io.File;
import java.util.List;

public class Arguments {
	@Parameter(names = {"-modFolder", "-folder", "-mods"}, description = "Folder where mods will be downloaded")
	public static String modFolder;
	@Parameter(names = {"-mcVersion"}, description = "Minecraft version")
	public static String mcVersion;
	@Parameter(names = {"-backupVersion", "-backupVersions"}, description = "Backup Minecraft version, during updates" +
			"when no version is found for the main Minecraft version, this version will be checked as well")
	public static List<String> backupVersions;
	@Parameter(names = {"-releaseType"}, description = "Release type for Curse updates. Acceptable parameters" +
			"are release, beta, and alpha", validateWith = ReleaseType.class)
	public static String releaseType;
	@Parameter(names = {"-maxDownloadThreads"}, description = "Max number of threads for downloading mods. The default is however many mods are in your manifest")
	public static int maxDownloadThreads;

	@Parameter(names = {"-forceDownload"}, description = "Forces downloading instead of pulling from the cache")
	public static boolean forceDownload = false;
	@Parameter(names = {"-generateUrlTxt"}, description = "Generates URL txt files for SKCraft Launcher." +
			"Currently not implemented")
	public static boolean generateUrlTxt;
	@Parameter(names = {"-updateForge"}, description = "Updates manifest to the latest version of Forge for the" +
			"specified Minecraft version")
	public static boolean updateForge;
	@Parameter(names = {"-updateCurseModPack"}, description = "Updates the Curse modpack instance")
	public static boolean updateCurseModPack;
	@Parameter(names = {"-updateApp"}, description = "Downloads latest version of ModPack Downloader")
	public static boolean updateApp;
	@Parameter(names = {"-clearCache"}, description = "Clears ModPack Downloader's cache")
	public static boolean clearCache;

	@Parameter(names = {"-manifest", "-manifests"}, description = "List of manifests to use for downloading/updating",
			listConverter = FileConverter.class, validateWith = ExistingFile.class)
	public static List<File> manifests;
	@Parameter(names = {"-downloadMods"}, description = "Downloads mods in the given manifests." +
			"Enabled by default if update and merge are not")
	public static boolean downloadMods;
	@Parameter(names = {"-updateMods"}, description = "Updates mods in the given manifests")
	public static boolean updateMods;
	@Parameter(names = {"-mergeManifests"}, description = "Merges the given manifests into one manifest")
	public static boolean mergeManifests;
	@Parameter(names = {"-help"}, description = "Displays this great message", help = true)
	public static boolean helpEnabled;
}

package com.nincraft.modpackdownloader.util;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.nincraft.modpackdownloader.validation.ExistingFile;
import com.nincraft.modpackdownloader.validation.ReleaseType;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

public class Arguments {
	@Getter
	@Setter
	@Parameter(names = {"-modFolder", "-folder", "-mods"}, description = "Folder where mods will be downloaded")
	private String modFolder;
	@Getter
	@Setter
	@Parameter(names = {"-mcVersion"}, description = "Minecraft version")
	private String mcVersion;
	@Getter
	@Parameter(names = {"-backupVersion", "-backupVersions"}, description = "Backup Minecraft version, during updates" +
			"when no version is found for the main Minecraft version, this version will be checked as well")
	private List<String> backupVersions;
	@Getter
	@Parameter(names = {"-releaseType"}, description = "Release type for Curse updates. Acceptable parameters" +
			"are release, beta, and alpha", validateWith = ReleaseType.class)
	private String releaseType;
	@Getter
	@Parameter(names = {"-maxDownloadThreads"}, description = "Max number of threads for downloading mods. The default is however many mods are in your manifest")
	private int maxDownloadThreads;
	@Getter
	@Parameter(names = {"-forceDownload"}, description = "Forces downloading instead of pulling from the cache")
	private boolean forceDownload = false;
	@Getter
	@Parameter(names = {"-generateUrlTxt"}, description = "Generates URL txt files for SKCraft Launcher." +
			"Currently not implemented")
	private boolean generateUrlTxt;
	@Getter
	@Parameter(names = {"-updateForge"}, description = "Updates manifest to the latest version of Forge for the" +
			"specified Minecraft version")
	private boolean updateForge;
	@Getter
	@Parameter(names = {"-updateCurseModPack"}, description = "Updates the Curse modpack instance, takes in a Curse modpack slug")
	private String updateCurseModPack;
	@Getter
	@Parameter(names = {"-updateApp"}, description = "Downloads latest version of ModPack Downloader")
	private boolean updateApp;
	@Getter
	@Parameter(names = {"-clearCache"}, description = "Clears ModPack Downloader's cache")
	private boolean clearCache;
	@Getter
	@Setter
	@Parameter(names = {"-manifest", "-manifests"}, description = "List of manifests to use for downloading/updating",
			listConverter = FileConverter.class, validateWith = ExistingFile.class)
	private List<File> manifests;
	@Getter
	@Setter
	@Parameter(names = {"-downloadMods"}, description = "Downloads mods in the given manifests." +
			"Enabled by default if update and merge are not")
	private boolean downloadMods;
	@Getter
	@Parameter(names = {"-updateMods"}, description = "Updates mods in the given manifests")
	private boolean updateMods;
	@Getter
	@Parameter(names = {"-checkMCUpdate"}, description = "Checks mods for updates given the Minecraft version (passed to this parameter) and manifests")
	private String checkMCUpdate;
	@Getter
	@Parameter(names = {"-mergeManifests"}, description = "Merges the given manifests into one manifest")
	private boolean mergeManifests;
	@Getter
	@Parameter(names = {"-help"}, description = "Displays this great message", help = true)
	private boolean helpEnabled;
}

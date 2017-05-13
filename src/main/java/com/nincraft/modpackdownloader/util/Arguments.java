package com.nincraft.modpackdownloader.util;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.nincraft.modpackdownloader.validation.ExistingFile;
import com.nincraft.modpackdownloader.validation.ReleaseType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor(access = PRIVATE)
public class Arguments {
	@Parameter(names = {"-modFolder", "-folder", "-mods"}, description = "Folder where mods will be downloaded")
	private String modFolder;
	@Parameter(names = {"-mcVersion"}, description = "Minecraft version")
	private String mcVersion;
	@Parameter(names = {"-backupVersion", "-backupVersions"}, description = "Backup Minecraft version, during updates" +
			"when no version is found for the main Minecraft version, this version will be checked as well")
	private List<String> backupVersions;
	@Parameter(names = {"-releaseType"}, description = "Release type for Curse updates. Acceptable parameters" +
			"are release, beta, and alpha", validateWith = ReleaseType.class)
	private String releaseType;
	@Parameter(names = {"-maxDownloadThreads"}, description = "Max number of threads for downloading mods. The default is however many mods are in your manifest")
	private int maxDownloadThreads;
	@Parameter(names = {"-forceDownload"}, description = "Forces downloading instead of pulling from the cache")
	private boolean forceDownload = false;
	@Parameter(names = {"-generateUrlTxt"}, description = "Generates URL txt files for SKCraft Launcher." +
			"Currently not implemented")
	private boolean generateUrlTxt;
	@Parameter(names = {"-updateForge"}, description = "Updates manifest to the latest version of Forge for the" +
			"specified Minecraft version")
	private boolean updateForge;
	@Parameter(names = {"-updateCurseModPack"}, description = "Updates the Curse modpack instance, takes in a Curse modpack slug")
	private String updateCurseModPack;
	@Parameter(names = {"-updateApp"}, description = "Downloads latest version of ModPack Downloader")
	private boolean updateApp;
	@Parameter(names = {"-clearCache"}, description = "Clears ModPack Downloader's cache")
	private boolean clearCache;
	@Parameter(names = {"-manifest", "-manifests"}, description = "List of manifests to use for downloading/updating",
			listConverter = FileConverter.class, validateWith = ExistingFile.class)
	private List<File> manifests;
	@Parameter(names = {"-downloadMods"}, description = "Downloads mods in the given manifests." +
			"Enabled by default if update and merge are not")
	private boolean downloadMods;
	@Parameter(names = {"-updateMods"}, description = "Updates mods in the given manifests")
	private boolean updateMods;
	@Parameter(names = {"-checkMCUpdate"}, description = "Checks mods for updates given the Minecraft version (passed to this parameter) and manifests")
	private String checkMCUpdate;
	@Parameter(names = {"-mergeManifests"}, description = "Merges the given manifests into one manifest")
	private boolean mergeManifests;
	@Parameter(names = {"-help"}, description = "Displays this great message", help = true)
	private boolean helpEnabled;
}

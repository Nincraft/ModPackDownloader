package com.nincraft.modpackdownloader.util;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

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

	@Parameter(names = { "-manifest", "-manifests", "-downloadMods" }, listConverter = FileConverter.class)
	public static List<File> manifestsToDownload;
	@Parameter(names = { "-updateMods" }, listConverter = FileConverter.class)
	public static List<File> manifestsToUpdate;
	@Parameter(names = { "-mergeManifests" }, listConverter = FileConverter.class)
	public static List<File> manifestsToMerge;

	public static boolean shouldDownloadManifests() {
		return CollectionUtils.isNotEmpty(manifestsToDownload);
	}

	public static boolean shouldUpdateManifests() {
		return CollectionUtils.isNotEmpty(manifestsToUpdate);
	}

	public static boolean shouldMergeManifests() {
		return CollectionUtils.isNotEmpty(manifestsToMerge);
	}
}

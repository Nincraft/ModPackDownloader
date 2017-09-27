package com.nincraft.modpackdownloader;

import com.google.gson.Gson;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.handler.ApplicationUpdateHandler;
import com.nincraft.modpackdownloader.util.VersionHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class ModpackDownloaderTest {

	private final String RESOURCES = "src/test/resources/";

	@After
	public void cleanUp() throws IOException {
		String[] deleteFiles = {RESOURCES + "update-test.json.bak", RESOURCES + "download-test.json.bak", RESOURCES + "update-test.json"};
		for (String s : deleteFiles) {
			new File(s).delete();
		}
		File backupUpdate = new File(RESOURCES + "update-test-backup.json");
		File updateFile = new File(RESOURCES + "update-test.json");
		FileUtils.copyFile(backupUpdate, updateFile);
	}

	@Test
	public void testDownloadRelease() throws InterruptedException {
		ModpackDownloaderManager manager = new ModpackDownloaderManager(new String[]{"-manifest", RESOURCES + "download-test.json", "-releaseType", "release", "-forceDownload"});
		manager.init();
		manager.processManifests();
		File mod;
		List<String> mods = new ArrayList<>(Arrays.asList("Thaumcraft-1.8.9-5.2.4.jar", "DimensionalDoors-2.2.5-test9.jar", "pants.jar", "forge-1.8.9-11.15.1.1902-1.8.9-installer.jar"));
		List<String> checkFiles = addMods(mods);

		for (String fileCheck : checkFiles) {
			mod = new File(fileCheck);
			Assert.assertTrue(mod.exists());
			mod.deleteOnExit();
		}
	}

	@Test
	public void testDownloadMaxThreads() throws InterruptedException {
		ModpackDownloaderManager manager = new ModpackDownloaderManager(new String[]{"-manifest", RESOURCES + "download-test.json", "-maxDownloadThreads", "1"});
		manager.init();
		manager.processManifests();
		File mod;
		List<String> mods = new ArrayList<>(Arrays.asList("Thaumcraft-1.8.9-5.2.4.jar", "DimensionalDoors-2.2.5-test9.jar", "pants.jar", "forge-1.8.9-11.15.1.1902-1.8.9-installer.jar"));
		List<String> checkFiles = addMods(mods);

		for (String fileCheck : checkFiles) {
			mod = new File(fileCheck);
			Assert.assertTrue(mod.exists());
			mod.deleteOnExit();
		}
	}

	@Test
	public void testUpdate() throws InterruptedException, IOException, ParseException {
		String manifestName = RESOURCES + "update-test.json";
		File manifestFile = new File(manifestName);
		String[] args = {"-manifest", manifestName, "-updateMods", "-updateForge", "-backupVersion", "1.8.9"};

		Gson gson = new Gson();
		JSONObject jsonLists = (JSONObject) new JSONParser().parse(new FileReader(manifestFile));
		Manifest manifest = gson.fromJson(jsonLists.toString(), Manifest.class);
		String oldForgeVersion = manifest.getForgeVersion();
		ModpackDownloaderManager manager = new ModpackDownloaderManager(args);
		manager.init();
		manager.processManifests();
		jsonLists = (JSONObject) new JSONParser().parse(new FileReader(manifestFile));
		manifest = gson.fromJson(jsonLists.toString(), Manifest.class);
		for (CurseFile curseFile : manifest.getCurseFiles()) {
			Assert.assertTrue(curseFile.getFileID() > 0);
		}
		Assert.assertTrue(VersionHelper.compareVersions(oldForgeVersion.substring(oldForgeVersion.indexOf('-') + 1),
				manifest.getForgeVersion().substring(manifest.getForgeVersion().indexOf('-') + 1)) < 0);
	}

	@Test
	public void testCheckUpdate() throws InterruptedException {
		String manifestName = RESOURCES + "update-test.json";
		String[] args = {"-manifest", manifestName, "-checkMCUpdate", "1.10.2"};
		ModpackDownloaderManager manager = new ModpackDownloaderManager(args);
		manager.init();
		manager.processManifests();
	}

	@Test
	public void testAppUpdate() {
		String[] args = {"-updateApp"};
		ModpackDownloaderManager manager = new ModpackDownloaderManager(args);
		manager.init();
		ApplicationUpdateHandler.update();
		FileFilter fileFilter = new WildcardFileFilter("ModpackDownloader*jar");
		File directory = new File(".");
		List<File> files = Arrays.asList(directory.listFiles(fileFilter));
		Assert.assertTrue(!CollectionUtils.isEmpty(files));
		files.forEach(File::deleteOnExit);
	}

	private List<String> addMods(List<String> mods) {
		String folder = "mods" + File.separator;
		return mods.stream().map(s -> folder + s).collect(Collectors.toList());
	}
}

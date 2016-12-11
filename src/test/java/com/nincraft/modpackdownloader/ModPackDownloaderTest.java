package com.nincraft.modpackdownloader;

import com.google.gson.Gson;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.util.Arguments;
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
import org.junit.Before;
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
public class ModPackDownloaderTest {

	@Before
	public void setup() {
		Arguments.updateMods = false;
		Arguments.updateApp = false;
	}

	@After
	public void cleanUp() throws IOException {
		String[] deleteFiles = {"src/test/resources/update-test.json.bak", "src/test/resources/download-test.json.bak", "src/test/resources/update-test.json"};
		for (String s : deleteFiles) {
			new File(s).delete();
		}
		File backupUpdate = new File("src/test/resources/update-test-backup.json");
		File updateFile = new File("src/test/resources/update-test.json");
		FileUtils.copyFile(backupUpdate, updateFile);
	}

	@Test
	public void testDownload() throws InterruptedException {
		String[] args = {"-manifest", "src/test/resources/download-test.json", "-releaseType", "release"};
		ModPackDownloader.main(args);
		File mod = null;
		List<String> mods = new ArrayList<>(Arrays.asList("Thaumcraft-1.8.9-5.2.4.jar", "DimensionalDoors-2.2.5-test9.jar", "pants.jar", "forge-1.8.9-11.15.1.1902-1.8.9-installer.jar"));
		List<String> checkFiles = addMods(mods);

		for (String fileCheck : checkFiles) {
			mod = new File(fileCheck);
			log.info("Checking " + mod + ": " + mod.exists());
			Assert.assertTrue(mod.exists());
			mod.deleteOnExit();
		}
	}

	@Test
	public void testUpdate() throws InterruptedException, IOException, ParseException {
		String manifestName = "src/test/resources/update-test.json";
		File manifestFile = new File(manifestName);
		String[] args = {"-manifest", manifestName, "-updateMods", "-updateForge", "-backupVersion", "1.8.9"};

		Gson gson = new Gson();
		JSONObject jsonLists = (JSONObject) new JSONParser().parse(new FileReader(manifestFile));
		Manifest manifest = gson.fromJson(jsonLists.toString(), Manifest.class);
		String oldForgeVersion = manifest.getForgeVersion();
		ModPackDownloader.main(args);
		jsonLists = (JSONObject) new JSONParser().parse(new FileReader(manifestFile));
		manifest = gson.fromJson(jsonLists.toString(), Manifest.class);
		for (CurseFile curseFile : manifest.getCurseFiles()) {
			Assert.assertTrue(curseFile.getFileID() > 0);
		}
		Assert.assertTrue(VersionHelper.compareVersions(oldForgeVersion.substring(oldForgeVersion.indexOf('-') + 1),
				manifest.getForgeVersion().substring(manifest.getForgeVersion().indexOf('-') + 1)) < 0);
	}

	@Test
	public void testAppUpdate() throws InterruptedException {
		String[] args = {"-updateApp"};
		ModPackDownloader.main(args);
		FileFilter fileFilter = new WildcardFileFilter("ModPackDownloader*jar");
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

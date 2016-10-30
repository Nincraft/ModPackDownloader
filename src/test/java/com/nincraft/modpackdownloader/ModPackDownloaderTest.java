package com.nincraft.modpackdownloader;

import com.google.gson.Gson;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModPackDownloaderTest {

	@AfterClass
	public static void cleanUp() throws IOException {
		File backupUpdate = new File("src/test/resources/update-test-backup.json");
		File updateFile = new File("src/test/resources/update-test.json");
		File updateFileBackup = new File("src/test/resources/update-test.json.bak");
		updateFile.delete();
		updateFileBackup.delete();
		FileUtils.copyFile(backupUpdate, updateFile);
	}

	@Test
	public void testDownload() throws InterruptedException {
		String[] args = {"-manifest", "src/test/resources/download-test.json"};
		ModPackDownloader.main(args);
		File mod = null;
		List<String> mods = new ArrayList<>(Arrays.asList("Thaumcraft-1.8.9-5.2.4.jar", "DimensionalDoors-2.2.5-test9.jar", "pants.jar"));
		List<String> checkFiles = addMods(mods);

		for (String fileCheck : checkFiles) {
			mod = new File(fileCheck);
			Assert.assertTrue(mod.exists());
			mod.deleteOnExit();
		}
	}

	@Test
	public void testUpdate() throws InterruptedException, IOException, ParseException {
		String manifestName = "src/test/resources/update-test.json";
		File manifestFile = new File(manifestName);
		String[] args = {"-manifest", manifestName, "-updateMods"};
		ModPackDownloader.main(args);
		Gson gson = new Gson();
		JSONObject jsonLists = (JSONObject) new JSONParser().parse(new FileReader(manifestFile));
		Manifest manifest = gson.fromJson(jsonLists.toString(), Manifest.class);
		for (CurseFile curseFile : manifest.getCurseFiles()) {
			Assert.assertTrue(curseFile.getFileID() > 0);
		}
	}

	private List<String> addMods(List<String> mods) {
		String folder = "mods" + File.separator;
		return mods.stream().map(s -> folder + s).collect(Collectors.toList());
	}
}

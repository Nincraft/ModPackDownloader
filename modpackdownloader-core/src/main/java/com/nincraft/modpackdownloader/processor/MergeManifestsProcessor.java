package com.nincraft.modpackdownloader.processor;

import com.google.common.collect.Sets;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.FileSystemHelper;
import com.nincraft.modpackdownloader.util.ManifestHelper;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Log4j2
public class MergeManifestsProcessor extends AbstractProcessor {
	private final Set<CurseFile> curseModSet;
	private final Manifest newManifest;

	public MergeManifestsProcessor(Arguments arguments, DownloadHelper downloadHelper) {
		super(arguments, downloadHelper);

		curseModSet = Sets.newHashSet();
		newManifest = new Manifest();
	}

	@Override
	protected void init(Map<File, Manifest> manifestMap) {
		// no-op
	}

	@Override
	public void process() throws InterruptedException {
		for(val entry : manifestMap.entrySet()) {
			process(entry);
		}

		newManifest.getCurseFiles().addAll(curseModSet);
		newManifest.getCurseFiles().sort(modComparator);

		newManifest.setOverrides("overrides");

		ManifestHelper.cleanupManifest(newManifest);
		FileSystemHelper.writeManifest(newManifest, "target/newManifest.json");
	}

	@Override
	protected boolean process(Entry<File, Manifest> manifestEntry) {
		val manifest = manifestEntry.getValue();

		processManifestHeaders(manifest);

		if (newManifest.getMinecraft() == null && manifest.getMinecraft() != null) {
			newManifest.setMinecraft(manifest.getMinecraft());
		}

		curseModSet.addAll(manifest.getCurseFiles());

		return true;
	}

	private void processManifestHeaders(Manifest manifest) {

		if (newManifest.getAuthor() == null) {
			newManifest.setAuthor(manifest.getAuthor());
		}

		if (newManifest.getManifestType() == null) {
			newManifest.setManifestType(manifest.getManifestType());
		}

		if (newManifest.getMinecraftVersion() == null) {
			newManifest.setManifestVersion(manifest.getManifestVersion());
		}

		if (newManifest.getName() == null) {
			newManifest.setName(manifest.getName());
		}

		if (newManifest.getVersion() == null) {
			newManifest.setVersion(manifest.getVersion());
		}
	}
}

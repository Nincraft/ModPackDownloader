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
	private final Manifest manifest;

	public MergeManifestsProcessor(Arguments arguments, DownloadHelper downloadHelper) {
		super(arguments, downloadHelper);

		curseModSet = Sets.newHashSet();
		manifest = new Manifest();
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

		manifest.getCurseFiles().addAll(curseModSet);
		manifest.getCurseFiles().sort(modComparator);

		manifest.setOverrides("overrides");

		ManifestHelper.cleanupManifest(manifest);
		FileSystemHelper.writeManifest(manifest, "target/manifest.json");
	}

	@Override
	protected boolean process(Entry<File, Manifest> manifestEntry) {
		val manifest = manifestEntry.getValue();

		if (this.manifest.getMinecraft() == null && manifest.getMinecraft() != null) {
			this.manifest.setMinecraft(manifest.getMinecraft());
		}

		curseModSet.addAll(manifest.getCurseFiles());

		return true;
	}
}

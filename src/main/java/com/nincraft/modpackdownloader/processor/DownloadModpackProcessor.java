package com.nincraft.modpackdownloader.processor;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nincraft.modpackdownloader.container.Manifest;

public class DownloadModpackProcessor extends AbstractProcessor {

	public DownloadModpackProcessor(final List<File> manifestFiles) {
		super(manifestFiles);
	}

	@Override
	protected void init(final Map<File, Manifest> manifestMap) {
	}

	@Override
	protected void preprocess(final Entry<File, Manifest> manifest) {
	}

	@Override
	protected void process(final Entry<File, Manifest> manifest) throws InterruptedException {
	}

	@Override
	protected void postProcess(final Entry<File, Manifest> manifest) {
	}
}

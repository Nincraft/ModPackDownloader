package com.nincraft.modpackdownloader.processor;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nincraft.modpackdownloader.container.Manifest;

public class MergeManifestsProcessor extends AbstractProcessor {
	public MergeManifestsProcessor(final List<File> manifestFiles) {
		super(manifestFiles);
	}

	@Override
	protected void init(final Map<File, Manifest> manifestFiles) {
		// no-op
	}

	@Override
	protected void preprocess(final Entry<File, Manifest> manifest) {
		// no-op
	}

	@Override
	protected void process(final Entry<File, Manifest> manifest) {
		// no-op
	}

	@Override
	protected void postProcess(final Entry<File, Manifest> manifest) {
		// no-op
	}
}

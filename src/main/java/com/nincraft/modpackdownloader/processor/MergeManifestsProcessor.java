package com.nincraft.modpackdownloader.processor;

import com.nincraft.modpackdownloader.container.Manifest;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MergeManifestsProcessor extends AbstractProcessor {
	public MergeManifestsProcessor(final List<File> manifestFiles) {
		super(manifestFiles);
	}

	@Override
	protected void init(Map<File, Manifest> manifestMap) {
		//no-op
	}

}

package com.nincraft.modpackdownloader.processor;

import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;

import java.io.File;
import java.util.Map;

public class MergeManifestsProcessor extends AbstractProcessor {
	public MergeManifestsProcessor(Arguments arguments, DownloadHelper downloadHelper) {
		super(arguments, downloadHelper);
	}

	@Override
	protected void init(Map<File, Manifest> manifestMap) {
		//no-op
	}

}

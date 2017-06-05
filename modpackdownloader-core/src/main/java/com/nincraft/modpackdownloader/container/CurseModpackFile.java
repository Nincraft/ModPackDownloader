package com.nincraft.modpackdownloader.container;

import com.nincraft.modpackdownloader.util.Reference;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class CurseModpackFile extends CurseFile {

	private Reference reference = Reference.getInstance();

	public CurseModpackFile(String projectId, String projectName, int fileId) {
		super(projectId, projectName);
		setFileID(fileId);
	}

	@Override
	public String getCurseforgeWidgetJson() {
		return reference.getCurseforgeWidgetJsonModpack();
	}
}

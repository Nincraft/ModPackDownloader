package com.nincraft.modpackdownloader.container;

import com.nincraft.modpackdownloader.util.Reference;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class CurseModpackFile extends CurseFile {

	private Reference reference = Reference.getInstance();

	public CurseModpackFile() {
		super();
		setFileID(0);
	}

	public CurseModpackFile(String projectId, String projectName) {
		super(projectId, projectName);
		setFileID(0);
	}

	@Override
	public String getCurseforgeWidgetJson() {
		return reference.getCurseforgeWidgetJsonModpack();
	}
}

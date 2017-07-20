package com.nincraft.modpackdownloader.container;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Generated;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Generated("org.jsonschema2pojo")
@Log4j2
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"fileID"})
public class CurseFile extends Mod {

	@SerializedName("fileID")
	@Expose
	public Integer fileID;
	@SerializedName("projectID")
	@Expose
	public Integer projectID;
	@SerializedName("release")
	@Expose
	public String releaseType;
	@SerializedName("skipUpdate")
	@Expose
	private Boolean skipUpdate;
	private String projectUrl;
	private String projectName;
	private boolean curseForge;
	private Reference reference = Reference.getInstance();

	public CurseFile() {
		curseForge = true;
	}

	public CurseFile(CurseFile curseFile) {
		super(curseFile);
		fileID = curseFile.fileID;
		projectID = curseFile.projectID;
		releaseType = curseFile.releaseType;
		skipUpdate = curseFile.skipUpdate;
		projectUrl = curseFile.projectUrl;
		projectName = curseFile.projectName;
		curseForge = curseFile.curseForge;
	}

	public CurseFile(String projectId, String projectName) {
		if (NumberUtils.isParsable(projectId)) {
			setProjectID(Integer.parseInt(projectId));
		}
		setProjectName(projectName);
	}

	public String getCurseforgeWidgetJson() {
		return reference.getCurseforgeWidgetJsonMod();
	}

	@Override
	public void init() {
		setProjectUrl(buildProjectUrl());

		try {
			if (Strings.isNullOrEmpty(getProjectName()) || Strings.isNullOrEmpty(getName())) {
				val conn = (HttpURLConnection) new URL(getProjectUrl()).openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.connect();

				if (Strings.isNullOrEmpty(getProjectName())) {
					setProjectName(conn.getHeaderField("Location").split("/")[2]);
				}

				if (Strings.isNullOrEmpty(getName())) {
					setName(getProjectName());
				}
			}
		} catch (final IOException e) {
			log.error(e);
		}
		setDownloadUrl(getDownloadUrl());

	}

	private String buildProjectUrl() {
		return String.format(reference.getCurseforgeBaseUrl() + "%s" + reference.getCookieTest1(), getProjectID());
	}

	public String getCurseForgeDownloadUrl() {
		String baseUrl = curseForge ? reference.getCurseforgeBaseUrl() : reference.getFtbBaseUrl();
		return String.format(baseUrl + "%s/files/%s/download", getProjectName(),
				getFileID());
	}
}
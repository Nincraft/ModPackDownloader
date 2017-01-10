package com.nincraft.modpackdownloader.container;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import javax.annotation.Generated;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Generated("org.jsonschema2pojo")
@Log4j2
@Data
@EqualsAndHashCode(callSuper = false)
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
	private boolean isModpack;
	private Reference reference = Reference.getInstance();

	public CurseFile() {
		//empty
	}

	public CurseFile(CurseFile curseFile) {
		super(curseFile);
		fileID = curseFile.fileID;
		projectID = curseFile.projectID;
		releaseType = curseFile.releaseType;
		skipUpdate = curseFile.skipUpdate;
		projectUrl = curseFile.projectUrl;
		projectName = curseFile.projectName;
		isModpack = curseFile.isModpack;
	}

	public CurseFile(String projectId, String projectName) {
		if (projectId != null) {
			setProjectID(Integer.parseInt(projectId));
		}
		setProjectName(projectName);
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

	public String buildProjectUrl() {
		return String.format(reference.getCurseforgeBaseUrl() + "%s" + reference.getCookieTest1(), getProjectID());
	}

	public String getCurseForgeDownloadUrl() {
		return getCurseForgeDownloadUrl(true);
	}

	public String getCurseForgeDownloadUrl(boolean isCurseForge) {
		String baseUrl = isCurseForge ? reference.getCurseforgeBaseUrl() : reference.getFtbBaseUrl();
		return String.format(baseUrl + "%s/files/%s/download", getProjectName(),
				getFileID());
	}

	public void initModpack() {
		init();
		setFileID(0);
		setModpack(true);
	}
}
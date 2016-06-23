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

	public CurseFile() {

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
			val conn = (HttpURLConnection) new URL(getProjectUrl()).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			if (Strings.isNullOrEmpty(getProjectName())) {
				setProjectName(conn.getHeaderField("Location").split("/")[2]);
			}

			if (Strings.isNullOrEmpty(getName())) {
				setName(getProjectName());
			}
		} catch (final IOException e) {
			log.error(e);
		}
		setDownloadUrl(getDownloadUrl());

	}

	public String buildProjectUrl() {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s" + Reference.COOKIE_TEST_1, getProjectID());
	}

	@Override
	public String getDownloadUrl() {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s-%s/files/%s/download", getProjectID(), getProjectName(),
				getFileID());
	}

	public void initModpack() {
		init();
		setFileID(0);
		setModpack(true);
	}
}
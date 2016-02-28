package com.nincraft.modpackdownloader.container;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;

import com.nincraft.modpackdownloader.util.Reference;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@EqualsAndHashCode(callSuper = false)
public class CurseMod extends ModContainer {
	private Long fileId;
	private Long projectId;
	private String projectName;
	private String projectUrl;

	public CurseMod() {
	}

	public CurseMod(final JSONObject modJson) {
		super(modJson);
		setProjectId((Long) modJson.get("projectID"));
		setFileId((Long) modJson.get("fileID"));
		setProjectUrl(buildProjectUrl());
		setDownloadUrl(getDownloadUrl());

		try {
			val conn = (HttpURLConnection) new URL(getProjectUrl()).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			setFolder(Reference.modFolder);
			setProjectName(conn.getHeaderField("Location").split("/")[2]);
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
	}

	public String buildProjectUrl() {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s" + Reference.COOKIE_TEST_1, getProjectId());
	}

	public String getAlternateDownloadUrl(final JSONObject modJson) {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s/files/%s/download", getProjectName(), getFileId());
	}

	@Override
	public String getDownloadUrl() {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s-%s/files/%s/download", getProjectId(),
				getProjectName(), getFileId());
	}

	@Override
	public CurseMod clone() throws CloneNotSupportedException {
		return (CurseMod) super.clone();
	}
}

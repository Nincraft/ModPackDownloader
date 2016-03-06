package com.nincraft.modpackdownloader.container;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nincraft.modpackdownloader.util.Reference;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import lombok.extern.log4j.Log4j2;

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
	private String projectUrl;
	private String projectName;

	public CurseFile() {

	}

	public void init() {
		setProjectUrl(buildProjectUrl());

		try {
			val conn = (HttpURLConnection) new URL(getProjectUrl()).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			setProjectName(conn.getHeaderField("Location").split("/")[2]);
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
		setDownloadUrl(getDownloadUrl());

	}

	public String buildProjectUrl() {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s" + Reference.COOKIE_TEST_1, getProjectID());
	}

	@Override
	public String getDownloadUrl() {
		return String.format(Reference.CURSEFORGE_BASE_URL + "%s-%s/files/%s/download", getProjectID(),
				getProjectName(), getFileID());
	}

}
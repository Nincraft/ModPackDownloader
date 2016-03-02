package com.nincraft.modpackdownloader.mapper;

import org.json.simple.JSONObject;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseMod;

import lombok.val;

public class CurseModMapper {
	@SuppressWarnings("unchecked")
	public static JSONObject map(final CurseMod mod) {
		val json = new JSONObject();

		json.put("name", mod.getModName());
		json.put("projectID", mod.getProjectId());
		json.put("fileID", mod.getFileId());

		if (!Strings.isNullOrEmpty(mod.getRename())) {
			json.put("rename", mod.getRename());
		}

		return json;
	}

	public static CurseMod map(final JSONObject json) {
		return new CurseMod(json);
	}
}

package com.nincraft.modpackdownloader.mapper;

import org.json.simple.JSONObject;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.ThirdPartyMod;

import lombok.val;

public class ThirdPartyMapper {
	@SuppressWarnings("unchecked")
	public JSONObject map(final ThirdPartyMod mod) {
		val json = new JSONObject();

		json.put("name", mod.getModName());
		json.put("url", mod.getDownloadUrl());

		if (!Strings.isNullOrEmpty(mod.getRename())) {
			json.put("rename", mod.getRename());
		}

		return json;
	}

	public ThirdPartyMod map(final JSONObject json) {
		return new ThirdPartyMod(json);
	}
}

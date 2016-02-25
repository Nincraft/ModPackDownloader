package com.nincraft.modpackdownloader.container;

import org.json.simple.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ThirdPartyMod extends ModContainer {

	public ThirdPartyMod(final JSONObject modJson) {
		super(modJson);
	}

	@Override
	String buildUrl(final JSONObject modJson) {
		return modJson.get("url").toString();
	}
}

package com.nincraft.modpackdownloader.container;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Generated("org.jsonschema2pojo")
@Getter
public class ModLoader {

	@SerializedName("id")
	@Expose
	public String id;
	@SerializedName("primary")
	@Expose
	public Boolean primary;
	@SerializedName("folder")
	@Expose
	public String folder;
}
package com.nincraft.modpackdownloader.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Generated("org.jsonschema2pojo")
@Getter
public class Minecraft {

	@SerializedName("version")
	@Expose
	public String version;
	@SerializedName("modLoaders")
	@Expose
	public List<ModLoader> modLoaders = new ArrayList<ModLoader>();

}
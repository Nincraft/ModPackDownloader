package com.nincraft.modpackdownloader.container;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
@Getter
@Setter
public class Minecraft {

	@SerializedName("version")
	@Expose
	public String version;
	@SerializedName("modLoaders")
	@Expose
	public List<ModLoader> modLoaders = new ArrayList<ModLoader>();

}
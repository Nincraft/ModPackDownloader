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
public class Manifest {

	@SerializedName("minecraft")
	@Expose
	public Minecraft minecraft;
	@SerializedName("files")
	@Expose
	public List<CurseFile> curseFiles = new ArrayList<>();
	@SerializedName("thirdParty")
	@Expose
	public List<ThirdParty> thirdParty = new ArrayList<>();
	@SerializedName("batchAddCurse")
	@Expose
	public List<String> batchAddCurse = new ArrayList<>();
	@SerializedName("overrides")
	@Expose
	public String overrides;

	public String getMinecraftVersion() {
		if (minecraft != null) {
			return minecraft.getVersion();
		}
		return null;
	}

	public String getForgeVersion() {
		if (minecraft != null && !minecraft.getModLoaders().isEmpty()) {
			return minecraft.getModLoaders().get(0).getId();
		}
		return null;
	}

}

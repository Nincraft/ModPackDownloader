package com.nincraft.modpackdownloader.container;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Manifest {

	@SerializedName("minecraft")
	@Expose
	private Minecraft minecraft;
	@SerializedName("manifestType")
	@Expose
	private String manifestType;
	@SerializedName("manifestVersion")
	@Expose
	private Integer manifestVersion;
	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("version")
	@Expose
	private String version;
	@SerializedName("author")
	@Expose
	private String author;
	@SerializedName("installedAddons")
	@Expose
	private List<CurseAddon> curseAddons = new ArrayList<>();
	@SerializedName("thirdParty")
	@Expose
	private List<ThirdParty> thirdParty = new ArrayList<>();
	@SerializedName("batchAddCurse")
	@Expose
	private List<String> batchAddCurse = new ArrayList<>();
	@SerializedName("overrides")
	@Expose
	private String overrides;

	public String getMinecraftVersion() {
		if (minecraft != null) {
			return minecraft.getVersion();
		}
		return null;
	}

	public String getForgeVersion() {
		if (isMinecraftEmpty()) {
			return minecraft.getModLoaders().get(0).getId();
		}
		return null;
	}

	private boolean isMinecraftEmpty() {
		return minecraft != null && !minecraft.getModLoaders().isEmpty();
	}

}

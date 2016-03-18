package com.nincraft.modpackdownloader.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Generated("org.jsonschema2pojo")
@Getter
@Setter
public class Manifest {

	@SerializedName("curseFiles")
	@Expose
	public List<CurseFile> curseFiles = new ArrayList<CurseFile>();
	@SerializedName("files")
	public List<CurseFile> curseManifestFiles = new ArrayList<CurseFile>();
	@SerializedName("thirdParty")
	@Expose
	public List<ThirdParty> thirdParty = new ArrayList<ThirdParty>();

}

package com.nincraft.modpackdownloader.container;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"fileID"})
public class CurseFile extends Mod {

    @SerializedName("displayName")
    @Expose
    public String name;

    @SerializedName("id")
    @Expose
    public Integer fileID;

    @SerializedName("release")
    @Expose
    public String releaseType;

    @SerializedName("skipUpdate")
    @Expose
    private Boolean skipUpdate;

    private Integer parentAddonId;
    private String parentAddonName;

    private String projectUrl;
    private String projectName;
    private String fileExtension;
    private Reference reference = Reference.getInstance();

    public CurseFile() {
        fileExtension = reference.getJarFileExt();
    }

    public CurseFile(CurseFile curseFile) {
        super(curseFile);
        fileID = curseFile.fileID;
        releaseType = curseFile.releaseType;
        skipUpdate = curseFile.skipUpdate;
        projectUrl = curseFile.projectUrl;
        projectName = curseFile.projectName;
        fileExtension = curseFile.fileExtension;
        parentAddonId = curseFile.parentAddonId;
        parentAddonName = curseFile.parentAddonName;
    }

    public CurseFile(Integer addonId, String addonName) {
        setParentAddonId(addonId);
        setParentAddonName(addonName);
    }


    public String getCurseforgeWidgetJson() {
        return reference.getCurseforgeWidgetJsonMod();
    }

    @Override
    public void init() {
    }
}

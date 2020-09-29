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

    @SerializedName("addonID")
    @Expose
    public Integer fileID;

    @SerializedName("releaseType")
    @Expose
    public Integer releaseType;

    @SerializedName("skipUpdate")
    @Expose
    private Boolean skipUpdate;

    @SerializedName("FileNameOnDisk")
    @Expose
    private String fileName;

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
    }

    public CurseFile(String projectId, String projectName) {
        /*if (NumberUtils.isParsable(projectId)) {
            setProjectID(Integer.parseInt(projectId));
        }*/
        setProjectName(projectName);
        /*curseForge = true;*/
    }

    public String getCurseforgeWidgetJson() {
        return reference.getCurseforgeWidgetJsonMod();
    }

    @Override
    public void init() {
        String fileName = getFileName();
        setSkipDownload(fileName != null && fileName.endsWith("disabled"));
    }
}

package com.nincraft.modpackdownloader.container;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.parser.ParseException;

import java.io.IOException;

@Log4j2
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"fileID"})
public class CurseFile extends Mod {

    @SerializedName("fileID")
    @Expose
    public Integer fileID;
    @SerializedName("projectID")
    @Expose
    public Integer projectID;
    @SerializedName("release")
    @Expose
    public String releaseType;
    @SerializedName("skipUpdate")
    @Expose
    private Boolean skipUpdate;
    private String projectUrl;
    private String projectName;
    private boolean curseForge;
    private String fileExtension;
    private Reference reference = Reference.getInstance();

    public CurseFile() {
        curseForge = true;
        fileExtension = reference.getJarFileExt();
    }

    public CurseFile(CurseFile curseFile) {
        super(curseFile);
        fileID = curseFile.fileID;
        projectID = curseFile.projectID;
        releaseType = curseFile.releaseType;
        skipUpdate = curseFile.skipUpdate;
        projectUrl = curseFile.projectUrl;
        projectName = curseFile.projectName;
        curseForge = curseFile.curseForge;
        fileExtension = curseFile.fileExtension;
    }

    public CurseFile(String projectId, String projectName) {
        if (NumberUtils.isParsable(projectId)) {
            setProjectID(Integer.parseInt(projectId));
        }
        setProjectName(projectName);
        curseForge = true;
    }

    public String getCurseforgeWidgetJson() {
        return reference.getCurseforgeWidgetJsonMod();
    }

    @Override
    public void init() {
        resolveProjectName();
        setProjectUrl(buildProjectUrl());
        setDownloadUrl(getCurseForgeDownloadUrl());
    }

    private void resolveProjectName() {
        if (Strings.isNullOrEmpty(getProjectName()) || Strings.isNullOrEmpty(getName())) {
            try {
                val response = URLHelper.getJsonFromUrl("https://addons-ecs.forgesvc.net/api/v2/addon/" + projectID);
                val newProjectName = response.get("slug").toString();

                if (Strings.isNullOrEmpty(getProjectName())) {
                    setProjectName(newProjectName);
                }

                if (Strings.isNullOrEmpty(getName())) {
                    setName(newProjectName);
                }
            } catch (IOException | ParseException e) {
                log.error(e);
            }
        }
    }

    private String buildProjectUrl() {
        return String.format(reference.getCurseforgeBaseUrl() + "%s" + reference.getCookieTest1(), getProjectID());
    }

    public String getCurseForgeDownloadUrl() {
        val baseUrl = curseForge ? reference.getCurseForgeBaseDownloadUrl() : reference.getFtbBaseUrl();
        return String.format(baseUrl + "%s/download/%s/file", getProjectName(), getFileID());
    }
}

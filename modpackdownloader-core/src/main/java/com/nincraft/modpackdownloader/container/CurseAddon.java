package com.nincraft.modpackdownloader.container;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public class CurseAddon {

    @SerializedName("addonID")
    @Expose
    public Integer addonID;

    @SerializedName("installedFile")
    @Expose
    public CurseFile installedFile;

    @SerializedName("projectName")
    @Expose
    public String projectName;

    public CurseAddon() {
    }
}

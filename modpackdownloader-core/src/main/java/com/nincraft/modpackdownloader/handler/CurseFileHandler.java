package com.nincraft.modpackdownloader.handler;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.summary.UpdateCheckSummarizer;
import com.nincraft.modpackdownloader.util.Arguments;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.Reference;
import com.nincraft.modpackdownloader.util.URLHelper;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public class CurseFileHandler implements ModHandler {

    private final Reference reference = Reference.getInstance();
    private final UpdateCheckSummarizer updateCheckSummarizer = UpdateCheckSummarizer.getInstance();
    private final Arguments arguments;
    private final DownloadHelper downloadHelper;

    public CurseFileHandler(Arguments arguments, DownloadHelper downloadHelper) {
        this.arguments = arguments;
        this.downloadHelper = downloadHelper;
    }

    private void downloadCurseMod(CurseFile curseFile) {
        try {
            downloadHelper.downloadFile(curseFile);
        } catch (Exception e) {
            log.error(e);
        }
    }

    /*public CurseFile getCurseForgeDownloadLocation(final CurseFile curseFile) throws IOException, ParseException {
        val url = curseFile.getDownloadUrl();
        val projectName = curseFile.getName();
        String encodedFilename = URLEncoder.encode(projectName, UTF_8);

        if (!encodedFilename.contains(reference.getJarFileExt())
                || !encodedFilename.contains(reference.getZipFileExt())) {
            val fileInfoResponse = URLHelper.getJsonFromUrl(String.format("https://addons-ecs.forgesvc"
                    + ".net/api/v2/addon/%s/file/%s", curseFile
                    .getProjectID(), curseFile.getFileID()));
            val fileName = fileInfoResponse.get("fileName").toString();
            val downloadUrl = fileInfoResponse.get("downloadUrl").toString();
            curseFile.setDownloadUrl(downloadUrl);
            encodedFilename = getEncodedFilename(projectName, fileName);
        }
        curseFile.setFileName(encodedFilename);

        return curseFile;
    }*/

    private String getEncodedFilename(String projectName, String filename) {
        if (filename.contains(reference.getJarFileExt()) || filename.contains(reference.getZipFileExt())) {
            return filename;
        } else {
            return projectName + reference.getJarFileExt();
        }
    }

    public void updateCurseFile(final CurseFile curseFile) {
        if (BooleanUtils.isTrue(curseFile.getSkipUpdate())) {
            log.debug("Skipped updating {}", curseFile.getName());
            return;
        }
        JSONObject fileListJson = new JSONObject();
        try {
            fileListJson.put("curse", getCurseProjectJson(curseFile).get("files"));

            if (fileListJson.get("curse") == null) {
                log.error("No file list found for {}, and will be skipped.", curseFile.getName());
                return;
            }
        } catch (IOException | ParseException e) {
            log.error(e);
            return;
        }

        val newMod = getLatestVersion(curseFile.getReleaseType() != null
                ? curseFile.getReleaseType()
                : arguments.getReleaseType(), curseFile, fileListJson, null);
        if (curseFile.getFileID().compareTo(newMod.getFileID()) < 0) {
            log.debug("Update found for {}.  Most recent version is {}.", curseFile.getName(), newMod.getVersion());
            updateCurseFile(curseFile, newMod);
            updateCheckSummarizer.getModList().add(curseFile);
        }
    }

    private void disableCertValidation() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    private void updateCurseFile(CurseFile curseFile, CurseFile newMod) {
        curseFile.setFileID(newMod.getFileID());
        curseFile.setName(newMod.getName());
        curseFile.setFileName(newMod.getFileName());
        curseFile.setVersion(newMod.getVersion());
        curseFile.setDownloadUrl(newMod.getDownloadUrl());
        /*if (curseFile instanceof CurseModpackFile) {
            curseFile.setFileName(newMod.getVersion());
        }*/
    }

    private CurseFile getLatestVersion(String releaseType,
            CurseFile curseFile, final JSONObject fileListJson, String mcVersion) {
        log.trace("Getting most recent available file...");
        boolean backup = true;
        if (Strings.isNullOrEmpty(mcVersion)) {
            mcVersion = arguments.getMcVersion();
            backup = false;
        }
        releaseType = defaultReleaseType(releaseType);
        CurseFile newMod = new CurseFile(curseFile);
        curseFile = checkFileId(curseFile);

        JSONArray fileList = (JSONArray) fileListJson.get("curse");
        List<Long> fileIds = new ArrayList<>();
        checkFileIds(releaseType, mcVersion, fileList, fileIds);
        Collections.sort(fileIds);
        Collections.reverse(fileIds);
        setUpdatedFileId(curseFile, fileList, newMod, fileIds);

        if (!"alpha".equals(releaseType) && fileIds.isEmpty()) {
            if (CollectionUtils.isEmpty(arguments.getBackupVersions())) {
                log.debug("No files found for Minecraft {}, disabling download of {}", mcVersion, curseFile.getName());
                curseFile.setSkipDownload(true);
            } else if (!backup) {
                newMod = checkBackupVersions(releaseType, curseFile, fileListJson, mcVersion, newMod);
            } else if (fileIds.isEmpty()) {
                curseFile.setSkipDownload(true);
                newMod.setSkipDownload(true);
            }
        }
        if (BooleanUtils.isTrue(curseFile.getSkipDownload()) && !fileIds.isEmpty()) {
            log.debug("Found files for Minecraft {}, enabling download of {}", mcVersion, curseFile.getName());
            newMod.setSkipDownload(null);
        }

        log.trace("Finished getting most recent available file.");
        return newMod;
    }

    private void setUpdatedFileId(CurseFile curseFile, JSONArray fileListJson, CurseFile newMod, List<Long> fileIds) {
        if (!fileIds.isEmpty()) {
            Long fileId = fileIds.get(0);
            if (fileId.intValue() > curseFile.getFileID()) {
                newMod.setFileID(fileId.intValue());

                val fileNode = getJSONFileNode(fileListJson, newMod.getFileID());
                val fileIdStr = String.valueOf(fileId);
                val fileName = fileNode.get("name").toString();

                newMod.setName(fileNode.get("display").toString());
                newMod.setFileName(fileName);
                newMod.setVersion(fileNode.get("version").toString());

                newMod.setDownloadUrl(String.format("https://edge.forgecdn.net/files/%s/%s/%s",
                        fileIdStr.substring(0, 4), fileIdStr.substring(4), fileName));
            }
        }
    }

    private JSONObject getJSONFileNode(JSONArray fileListJson, Integer fileID) {
        for (val jsonNode : fileListJson) {
            if (fileID.equals(((Long) ((JSONObject) jsonNode).get("id")).intValue())) {
                return (JSONObject) jsonNode;
            }
        }
        return new JSONObject();
    }

    private void checkFileIds(String releaseType, String mcVersion, List<JSONObject> fileList, List<Long> fileIds) {
        for (JSONObject file : fileList) {
            if (equalOrLessThan((String) file.get("type"), releaseType)
                    && isMcVersion((String) file.get("version"), mcVersion)) {
                fileIds.add((Long) file.get("id"));
            }
        }
    }

    private String defaultReleaseType(String releaseType) {
        return releaseType != null ? releaseType : "release";
    }

    private CurseFile checkBackupVersions(String releaseType, CurseFile curseFile, JSONObject fileListJson,
            String mcVersion, CurseFile newMod) {
        CurseFile returnMod = newMod;
        for (String backupVersion : arguments.getBackupVersions()) {
            log.debug("No files found for Minecraft {}, checking backup version {}", mcVersion, backupVersion);
            returnMod = getLatestVersion(releaseType, curseFile, fileListJson, backupVersion);
            if (BooleanUtils.isFalse(newMod.getSkipDownload())) {
                curseFile.setSkipDownload(null);
                log.debug("Found update for {} in Minecraft {}", curseFile.getName(), backupVersion);
                break;
            }
        }
        return returnMod;
    }

    private boolean isMcVersion(String modVersion, String argVersion) {
        return "*".equals(argVersion) || modVersion.equals(argVersion);
    }

    private CurseFile checkFileId(CurseFile curseFile) {
        if (curseFile.getFileID() == null) {
            curseFile.setFileID(0);
        }
        return curseFile;
    }

    private boolean equalOrLessThan(final String modRelease, final String releaseType) {
        return "alpha".equals(releaseType) || releaseType.equals(modRelease)
                || "beta".equals(releaseType) && "release".equals(modRelease);

    }

    private JSONObject getCurseProjectJson(final CurseFile curseFile) throws ParseException, IOException {
        log.trace("Getting CurseForge Widget JSON...");

        val projectId = curseFile.getParentAddonId();
        val projectName = curseFile.getProjectName();
        val modOrModPack = curseFile.getCurseforgeWidgetJson();

        String urlStr = String.format(reference.getCurseforgeWidgetJsonUrl(), modOrModPack, projectName != null && !projectName.isBlank() ? projectName : projectId);
        log.debug(urlStr);

        disableCertValidation();
        try {
            return URLHelper.getJsonFromUrl(urlStr);
        } catch (final FileNotFoundException e) {
            urlStr = String.format(reference.getCurseforgeWidgetJsonUrl(), modOrModPack, projectId);
            log.debug(urlStr, e);
            return URLHelper.getJsonFromUrl(urlStr);
        }
    }

    @Override
    public void downloadMod(final Mod mod) {
        downloadCurseMod((CurseFile) mod);
    }

    @Override
    public void updateMod(final Mod mod) {
         updateCurseFile((CurseFile) mod);
    }
}

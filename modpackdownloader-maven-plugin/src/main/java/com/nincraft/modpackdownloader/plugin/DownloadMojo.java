package com.nincraft.modpackdownloader.plugin;

import com.nincraft.modpackdownloader.ModpackDownloaderManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "download")
public class DownloadMojo extends AbstractMojo {

    @Parameter(property = "mpd.manifest", defaultValue = "manifest.json")
    private String manifest;

    @Parameter(property = "mpd.modfolder", defaultValue = "mods")
    private String modFolder;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Starting modpack download");
        String[] args = {"-manifest", manifest, "-modFolder", modFolder};
        try {
			ModpackDownloaderManager modpackDownloaderManager = new ModpackDownloaderManager(args);
			modpackDownloaderManager.init();
			modpackDownloaderManager.processManifests();
		} catch (InterruptedException e) {
            getLog().error("Download interrupted", e);
        }
    }
}

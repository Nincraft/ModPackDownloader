package com.nincraft.modpackdownloader.plugin;

import com.nincraft.modpackdownloader.ModPackDownloader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "update")
public class UpdateMojo extends AbstractMojo {

    @Parameter(property = "mpd.manifest", defaultValue = "manifest.json")
    private String manifest;

    @Parameter(property = "mpd.modfolder", defaultValue = "mods")
    private String modFolder;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Starting modpack update");
        String[] args = {"-manifest", manifest, "-modFolder", modFolder, "-updateMods"};
        try {
            ModPackDownloader.main(args);
        } catch (InterruptedException e) {
            getLog().error("Update interrupted", e);
        }
    }
}

package com.nincraft.modpackdownloader.gui;

import com.nincraft.modpackdownloader.ModpackDownloaderManager;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Controller {
	@FXML
	private TextField projectIdTextBox;
	@FXML
	private TextField fileIdTextBox;
	@FXML
	private Button downloadButton;
	@FXML
	private Hyperlink reportIssue;
	@FXML
	private Hyperlink help;
	private HostServices hostServices;

	void init() {
		help.setOnAction(event -> hostServices.showDocument("https://github.com/Nincraft/ModPackDownloader/wiki/GUI-Usage"));
		reportIssue.setOnAction(event -> hostServices.showDocument("https://github.com/Nincraft/ModPackDownloader/issues/new"));
		downloadButton.setOnAction(event -> startDownload());
	}

	private void startDownload() {
		String projectUrl = projectIdTextBox.getText();
		String fileIdText = fileIdTextBox.getText();
		String projectIdName = projectUrl.substring(projectUrl.lastIndexOf('/') + 1);
		String[] args = {"-updateCurseModPack", projectIdName, "-curseFileId", fileIdText};
		ModpackDownloaderManager modpackDownloaderManager = new ModpackDownloaderManager(args);
		modpackDownloaderManager.init();
		try {
			modpackDownloaderManager.processManifests();
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	void setHostServices(HostServices hostServices) {
		this.hostServices = hostServices;
	}
}

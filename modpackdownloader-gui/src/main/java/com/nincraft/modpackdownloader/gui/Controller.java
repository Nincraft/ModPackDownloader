package com.nincraft.modpackdownloader.gui;

import com.nincraft.modpackdownloader.ModpackDownloaderManager;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class Controller {

	@FXML
	private ProgressBar progressBar;
	@FXML
	private TextArea logTextArea;
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
		progressBar.setProgress(0);
		help.setOnAction(event -> hostServices.showDocument("https://github.com/Nincraft/ModPackDownloader/wiki/GUI-Usage"));
		reportIssue.setOnAction(event -> hostServices.showDocument("https://github.com/Nincraft/ModPackDownloader/issues/new"));
		downloadButton.setOnAction(event -> startDownload());
		logTextArea.setEditable(false);
		TextAreaAppender.setTextArea(logTextArea);
	}

	private void startDownload() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			try {
				String projectUrl = projectIdTextBox.getText();
				String fileIdText = fileIdTextBox.getText();
				String projectIdName = projectUrl.substring(projectUrl.lastIndexOf('/') + 1);
				String[] args = {"-updateCurseModPack", projectIdName, "-curseFileId", fileIdText};
				ModpackDownloaderManager modpackDownloaderManager = new ModpackDownloaderManager(args);
				modpackDownloaderManager.init();
				modpackDownloaderManager.processManifests();
			} catch (InterruptedException e) {
				log.error(e);
				executor.shutdownNow();
			}
		});
		executor.shutdownNow();
	}

	void setHostServices(HostServices hostServices) {
		this.hostServices = hostServices;
	}
}

package com.nincraft.modpackdownloader.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ModpackDownloaderGUI extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("mpd.fxml"));
		Parent root = loader.load();
		Controller controller = loader.getController();
		controller.setHostServices(getHostServices());
		controller.init();
		primaryStage.setTitle("Modpack Downloader by Nincraft Team");
		primaryStage.setScene(new Scene(root, 600, 410));
		primaryStage.show();
	}
}

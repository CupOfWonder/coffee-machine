package com.parcel.coffee;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

	private static SceneSwitcher instance;

	private Stage stage;
	private Scene mainWindow;
	private Scene secondScene;

	private SceneSwitcher() {
		try {
			Parent mainWindowRoot = FXMLLoader.load(getClass().getClassLoader().getResource("MainApp.fxml"));
			mainWindow = new Scene(mainWindowRoot);
			mainWindow.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

			Parent secondSceneRoot = FXMLLoader.load(getClass().getClassLoader().getResource("LoginWindow.fxml"));
			secondScene = new Scene(secondSceneRoot);
			secondScene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static SceneSwitcher getInstance() {
		if(instance == null) {
			instance = new SceneSwitcher();
		}
		return instance;
	}

	public void registerStage(Stage stage) {
		this.stage = stage;
	}

	public void switchToMainWindow() {
		if(stage.isShowing()) {
			stage.close();
		}
		stage.setScene(mainWindow);
	}

	public void switchToSecondScene() {
		if(stage.isShowing()) {
			stage.close();
		}
		stage.setScene(secondScene);
		stage.setFullScreen(true);
		stage.show();
	}
}

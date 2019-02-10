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
	private Scene loginWindow;
	private Scene administrationWindow;
	private Scene changePasswordWindow;

	private SceneSwitcher() {
		try {
			Parent mainWindowRoot = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
			mainWindow = new Scene(mainWindowRoot);
			mainWindow.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

			Parent secondSceneRoot = FXMLLoader.load(getClass().getClassLoader().getResource("LoginWindow.fxml"));
			loginWindow = new Scene(secondSceneRoot);
			loginWindow.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

			Parent administrationRoot = FXMLLoader.load(getClass().getClassLoader().getResource("AdministrationPanel.fxml"));
			administrationWindow = new Scene(administrationRoot);
			administrationWindow.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

			Parent changePasswordRoot = FXMLLoader.load(getClass().getClassLoader().getResource("ChangePasswordWindow.fxml"));
			changePasswordWindow = new Scene(changePasswordRoot);
			changePasswordWindow.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());
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
		stage.show();
	}

	public void switchToLoginWindow() {
		if(stage.isShowing()) {
			stage.close();
		}
		stage.setScene(loginWindow);
		stage.setFullScreen(true);
		stage.show();
	}

	public void switchToAdministrationPanel() {
		if(stage.isShowing()) {
			stage.close();
		}
		stage.setScene(administrationWindow);
		stage.setFullScreen(true);
		stage.show();
	}

	public void switchToChangePasswordWindow() {
		if(stage.isShowing()) {
			stage.close();
		}
		stage.setScene(changePasswordWindow);
		stage.setFullScreen(true);
		stage.show();
	}


}

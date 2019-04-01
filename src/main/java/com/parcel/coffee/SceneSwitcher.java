package com.parcel.coffee;

import javafx.application.Platform;
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
			Parent mainWindowRoot = FXMLLoader.load(getClass().getClassLoader().getResource("ui/MainWindow.fxml"));
			mainWindow = new Scene(mainWindowRoot);
			mainWindow.getStylesheets().add(getClass().getClassLoader().getResource("ui/style.css").toExternalForm());

			Parent secondSceneRoot = FXMLLoader.load(getClass().getClassLoader().getResource("ui/LoginWindow.fxml"));
			loginWindow = new Scene(secondSceneRoot);
			loginWindow.getStylesheets().add(getClass().getClassLoader().getResource("ui/style.css").toExternalForm());

			Parent administrationRoot = FXMLLoader.load(getClass().getClassLoader().getResource("ui/AdministrationPanel.fxml"));
			administrationWindow = new Scene(administrationRoot);
			administrationWindow.getStylesheets().add(getClass().getClassLoader().getResource("ui/style.css").toExternalForm());

			Parent changePasswordRoot = FXMLLoader.load(getClass().getClassLoader().getResource("ui/ChangePasswordWindow.fxml"));
			changePasswordWindow = new Scene(changePasswordRoot);
			changePasswordWindow.getStylesheets().add(getClass().getClassLoader().getResource("ui/style.css").toExternalForm());
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
		switchToScene(mainWindow);
	}

	public void switchToLoginWindow() {
		switchToScene(loginWindow);
	}

	public void switchToAdministrationPanel() {
		switchToScene(administrationWindow);
	}

	public void switchToChangePasswordWindow() {
		switchToScene(changePasswordWindow);
	}

	private void switchToScene(Scene scene) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(stage.isShowing()) {
					stage.close();
				}
				stage.setScene(scene);
				stage.show();
			}
		});
	}

}

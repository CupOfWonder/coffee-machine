package com.parcel.coffee;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class MainApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		SceneSwitcher switcher = SceneSwitcher.getInstance();
		switcher.registerStage(stage);


		stage.setResizable(false);
		stage.sizeToScene();

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				System.exit(0);
			}
		});

		switcher.switchToMainWindow();
		stage.show();
		stage.setFullScreen(true);


	}


}
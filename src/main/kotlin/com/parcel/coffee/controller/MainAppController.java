package com.parcel.coffee.controller;

import com.parcel.coffee.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class MainAppController {

	@FXML
	public void initialize() {
	}

	public void onMouse(MouseEvent mouseEvent) {
		SceneSwitcher.getInstance().switchToSecondScene();
	}
}

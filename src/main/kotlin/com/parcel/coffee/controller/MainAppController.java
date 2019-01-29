package com.parcel.coffee.controller;

import com.parcel.coffee.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class MainAppController {

	@FXML
	public void initialize() {
	}

	public void onMouse(MouseEvent mouseEvent) {
		if(mouseEvent.getClickCount() == 2) {
			SceneSwitcher.getInstance().switchToLoginWindow();
		}
	}

	public void onKeyPressed(KeyEvent keyEvent) {

	}
}

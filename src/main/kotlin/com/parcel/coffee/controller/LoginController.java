package com.parcel.coffee.controller;

import com.parcel.coffee.SceneSwitcher;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController {

	public void onKey(KeyEvent keyEvent) {
		if(keyEvent.getCode() == KeyCode.ESCAPE) {
			SceneSwitcher.getInstance().switchToMainWindow();
		}
	}
}

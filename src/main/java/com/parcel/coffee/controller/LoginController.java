package com.parcel.coffee.controller;

import com.parcel.coffee.SceneSwitcher;
import com.parcel.coffee.core.auth.PasswordManager;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController {

	public TextField loginField;

	public PasswordField passwordField;

	public Label loginError;

	public void onKey(KeyEvent keyEvent) {
		if(keyEvent.getCode() == KeyCode.ESCAPE) {
			SceneSwitcher.getInstance().switchToMainWindow();
		} else if (keyEvent.getCode() == KeyCode.ENTER) {
			tryToLogin();

		}
	}

	private void tryToLogin() {
		PasswordManager pm = new PasswordManager();

		String login = loginField.getText();
		String password = passwordField.getText();

		if(pm.checkLoginAndPassword(login, password)) {
			SceneSwitcher.getInstance().switchToAdministrationPanel();
		} else {
			showError();
		}
	}

	private void showError() {
		loginError.setManaged(true);
	}
}

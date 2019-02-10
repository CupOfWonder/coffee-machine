package com.parcel.coffee.controller;

import com.parcel.coffee.SceneSwitcher;
import com.parcel.coffee.core.auth.PasswordManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Timer;
import java.util.TimerTask;

public class ChangePasswordController {

	private static final long MESSAGE_SHOW_DURATION = 1000;
	public Label changeError;
	public TextField loginField, oldPasswordField, newPasswordField, newPasswordConfirmField;
	public Label messageLabel;

	private PasswordManager passwordManager = new PasswordManager();


	public void onChangeConfirm(MouseEvent mouseEvent) {
		doChangePassword();
	}

	public void onKey(KeyEvent keyEvent) {
		if(keyEvent.getCode() == KeyCode.ENTER) {
			doChangePassword();
		} else if(keyEvent.getCode() == KeyCode.ESCAPE) {
			SceneSwitcher.getInstance().switchToAdministrationPanel();
		}
	}

	private void doChangePassword() {
		String login = loginField.getText();
		String oldPassword = oldPasswordField.getText();
		String newPassword = newPasswordField.getText();
		String newPasswordConfirm = newPasswordConfirmField.getText();

		PasswordManager.PasswordChangeStatus status =
				passwordManager.changeLoginAndPassword(login, oldPassword, newPassword, newPasswordConfirm);

		switch (status) {
			case CHANGED_SUCCESSFULLY:
				showMessageAndDo("Пароль успешно изменен!", new Runnable() {
					@Override
					public void run() {
						SceneSwitcher.getInstance().switchToAdministrationPanel();
					}
				});
				break;
			case INCORRECT_OLD_PASSWORD:
				showMessage("Неверный старый пароль!");
				break;
			case CONFIRMATION_DOES_NOT_MATCH:
				showMessage("Пароль и подтверждение не совпадают!");
				break;
		}
	}

	private void showMessage(String message) {
		showMessageAndDo(message, null);
	}

	private void showMessageAndDo(String message, Runnable doAfter) {
		messageLabel.setText(message);
		messageLabel.setManaged(true);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						messageLabel.setText(null);
						messageLabel.setManaged(false);

						if(doAfter != null) {
							doAfter.run();
						}
					}
				});
			}
		}, MESSAGE_SHOW_DURATION);
	}

}

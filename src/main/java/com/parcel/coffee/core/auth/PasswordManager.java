package com.parcel.coffee.core.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.Nonnull;
import java.io.*;

public class PasswordManager {

	private static final String FILE_PATH = "passwd.json";

	public enum PasswordChangeStatus {
		CHANGED_SUCCESSFULLY, INCORRECT_OLD_PASSWORD, CONFIRMATION_DOES_NOT_MATCH
	}

	public PasswordManager() {
		if(!passwordFileExists()) {
			createDefaultPasswordFile();
		}
	}

	private boolean passwordFileExists() {
		File f = new File(FILE_PATH);
		return f.exists() && !f.isDirectory();
	}

	private void createDefaultPasswordFile() {
		Credentials credentials = new Credentials("admin", "admin");
		saveCredentials(credentials);
	}

	public boolean checkLoginAndPassword(String testLogin, String testPassword) {
		Credentials rightCred = readCredentialsFromFile();
		return loginAndPasswordCorrect(testLogin, testPassword, rightCred);

	}

	private boolean loginAndPasswordCorrect(String login, String password, Credentials rightCred) {
		if(login == null || password == null) {
			return false;
		}

		return login.equals(rightCred.getLogin()) && BCrypt.checkpw(password, rightCred.getEncryptedPassword());
	}

	public PasswordChangeStatus changeLoginAndPassword(String login, String oldPassword, String newPassword, String confirmation) {
		Credentials rightCred = readCredentialsFromFile();

		if(!loginAndPasswordCorrect(login, oldPassword, rightCred)) {
			return PasswordChangeStatus.INCORRECT_OLD_PASSWORD;
		}

		if(!newPassword.equals(confirmation)) {
			return PasswordChangeStatus.CONFIRMATION_DOES_NOT_MATCH;
		}

		Credentials newCred = new Credentials(login, newPassword);

		saveCredentials(newCred);
		return PasswordChangeStatus.CHANGED_SUCCESSFULLY;
	}

	private void saveCredentials(Credentials credentials) {
		File myFile = new File(FILE_PATH);

		try(BufferedWriter bf = new BufferedWriter(new FileWriter(myFile, false))){
			Gson gs = new GsonBuilder().setPrettyPrinting().create();
			String json = gs.toJson(credentials);
			bf.write(json);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Nonnull
	private Credentials readCredentialsFromFile() {
		File passwordFile = new File(FILE_PATH);

		try(BufferedReader br = new BufferedReader(new FileReader(passwordFile))) {
			StringBuilder builder = new StringBuilder();
			br.lines().forEach(builder::append);

			Gson g = new Gson();
			return g.fromJson(builder.toString(), Credentials.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Cannot read password file!");
	}
}

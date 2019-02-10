package com.parcel.coffee.core.auth;

import org.mindrot.jbcrypt.BCrypt;

public class Credentials {

    private String login;
    private String encryptedPassword;

    public Credentials(String login, String password) {
        this.login = login;
        this.encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public String getLogin() {
        return login;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }
}


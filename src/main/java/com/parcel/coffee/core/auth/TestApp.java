package com.parcel.coffee.core.auth;

public class TestApp {

    public static void main(String[] args) {

        PasswordManager m = new PasswordManager();

        m.checkLoginAndPassword("Person","Person_1995");

        m.changeLoginAndPassword("Person","Person_1","Person_1995", "Person_1996");
    }
}

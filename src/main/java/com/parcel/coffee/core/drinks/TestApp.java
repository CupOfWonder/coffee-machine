package com.parcel.coffee.core.drinks;

public class TestApp {

    public static void main(String[] args) {

        DrinkListSaver m = new DrinkListSaver();

        m.createDefaultDrinkList();

        m.loadCurrentPricesAndTitles();
    }
}
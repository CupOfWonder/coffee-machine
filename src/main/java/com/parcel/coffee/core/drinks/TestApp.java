package com.parcel.coffee.core.drinks;

public class TestApp {

    public static void main(String[] args) {

        DrinkListManager m = new DrinkListManager();

        m.createDefaultDrinkList();

        m.loadCurrentPricesAndTitles();
    }
}
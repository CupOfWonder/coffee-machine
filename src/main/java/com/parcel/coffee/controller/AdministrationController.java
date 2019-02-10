package com.parcel.coffee.controller;

import com.parcel.coffee.SceneSwitcher;
import com.parcel.coffee.core.drinks.Drink;
import com.parcel.coffee.core.drinks.DrinkListManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AdministrationController {
	public TextField name1, price1, name2, price2, name3, price3,
			name4, price4, name5, price5, name6, price6;

	public Label infoText;

	private List<DrinkFieldPair> drinkFieldPairs = new ArrayList<>();

	private DrinkListManager drinkListManager = new DrinkListManager();

	@FXML
	public void initialize() {
		System.out.println("initialize!");

		drinkFieldPairs.add(new DrinkFieldPair(name1, price1));
		drinkFieldPairs.add(new DrinkFieldPair(name2, price2));
		drinkFieldPairs.add(new DrinkFieldPair(name3, price3));
		drinkFieldPairs.add(new DrinkFieldPair(name4, price4));
		drinkFieldPairs.add(new DrinkFieldPair(name5, price5));
		drinkFieldPairs.add(new DrinkFieldPair(name6, price6));

		readDrinksFromFile();
	}


	public void onSaveDrinks(MouseEvent mouseEvent) {
		try {
			List<Drink> drinks = readDrinksFromInterface();
			drinkListManager.savePricesAndTitles(drinks);

			showMessage("Изменения сохранены!", new Runnable() {
				@Override
				public void run() {
					SceneSwitcher.getInstance().switchToMainWindow();
				}
			});

		} catch (PriceFormatException e) {
			showPriceFormatError();
		}
	}

	private void readDrinksFromFile() {
		List<Drink> drinks = drinkListManager.loadCurrentPricesAndTitles();

		for(int i = 0; i < drinks.size(); i++) {
			Drink drink = drinks.get(i);
			DrinkFieldPair pair = drinkFieldPairs.get(i);

			pair.setDrinkName(drink.getName());
			pair.setPrice(drink.getPrice());
		}
	}

	private List<Drink> readDrinksFromInterface() throws PriceFormatException {

		List<Drink> drinks = new ArrayList<>();

		for (DrinkFieldPair pair : drinkFieldPairs) {
			drinks.add(new Drink(pair.getDrinkName(), pair.getDrinkPrice()));
		}
		return drinks;
	}

	private void showPriceFormatError() {
		showMessage("Проверьте формат цен!");
	}

	private void showMessage(String msg) {
		showMessage(msg, null);
	}

	private void showMessage(String msg, Runnable doAfter) {
		infoText.setManaged(true);
		infoText.setText(msg);

		Timer timer =  new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						infoText.setManaged(false);
						infoText.setText(null);

						if(doAfter != null) {
							doAfter.run();
						}
					}
				});
			}
		}, 1000);
	}

	private class PriceFormatException extends Exception {
		public PriceFormatException() {
			super();
		}
	}

	private class DrinkFieldPair {
		private TextField nameField;
		private TextField priceField;

		public DrinkFieldPair(TextField nameField, TextField priceField) {
			this.nameField = nameField;
			this.priceField = priceField;
		}

		public String getDrinkName() {
			return nameField.getText();
		}

		public int getDrinkPrice() throws PriceFormatException {
			String priceSrc = priceField.getText();
			try {
				Integer price = Integer.parseInt(priceSrc);
				if (price > 0 && price < 5000) {
					return price;
				} else {
					throw new PriceFormatException();
				}
			} catch (NumberFormatException e) {
				throw new PriceFormatException();
			}

		}

		public void setDrinkName(String name) {
			nameField.setText(name);
		}

		public void setPrice(int price) {
			priceField.setText(Integer.toString(price));
		}
	}

}

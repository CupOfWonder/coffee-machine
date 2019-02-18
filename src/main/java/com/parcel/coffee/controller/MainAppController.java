package com.parcel.coffee.controller;

import com.parcel.Board;
import com.parcel.coffee.SceneSwitcher;
import com.parcel.coffee.core.drinks.Drink;
import com.parcel.coffee.core.drinks.DrinkListManager;
import com.parcel.coffee.core.events.DrinkListChangeHandler;
import com.parcel.coffee.core.events.EventBus;
import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler;
import com.parcel.coffee.core.hardware.helpers.WorkFinishHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.*;

public class MainAppController {

	public Label name1, name2, name3, name4, name5, name6, price1, price2, price3, price4, price5, price6;
	public HBox drinkIsMakingLabel, drinkReadyLabel, balanceLabel;
	public HBox drinkPanel1, drinkPanel2, drinkPanel3, drinkPanel4, drinkPanel5, drinkPanel6;

	private List<DrinkLabelPair> drinkLabelPairs = new ArrayList<>();
	private Map<Integer, HBox> buttonPanelMap = new HashMap<>();

	private Board board = new Board();

	private boolean drinkIsBeingMaked = false;

	private Timer drinkMakingAnimationTimer = new Timer();

	private static final int DRINK_MAKING_BLINK_PERIOD = 700;
	private static final int DRINK_COMPLETE_SHOW_PERIOD = 1800;

	private Integer selectedDrink;

	@FXML
	public void initialize() {
		initUi();
		initHardware();
	}


	private void initUi() {
		drinkLabelPairs.add(new DrinkLabelPair(name1, price1));
		drinkLabelPairs.add(new DrinkLabelPair(name2, price2));
		drinkLabelPairs.add(new DrinkLabelPair(name3, price3));
		drinkLabelPairs.add(new DrinkLabelPair(name4, price4));
		drinkLabelPairs.add(new DrinkLabelPair(name5, price5));
		drinkLabelPairs.add(new DrinkLabelPair(name6, price6));

		buttonPanelMap.put(0, drinkPanel1);
		buttonPanelMap.put(1, drinkPanel2);
		buttonPanelMap.put(2, drinkPanel3);
		buttonPanelMap.put(3, drinkPanel4);
		buttonPanelMap.put(4, drinkPanel5);
		buttonPanelMap.put(5, drinkPanel6);


		EventBus.getInstance().addDrinkListChangeHandler(new DrinkListChangeHandler() {
			@Override
			public void onDrinkListChanged() {
				readLabelsFromFile();
			}
		});

		readLabelsFromFile();
	}

	private void initHardware() {
		board.generate();
		if(!board.update()) {
			board.save();
		}
		for(int buttonNum = 0; buttonNum < 6; buttonNum++) {

			int finalButtonNum = buttonNum;
			board.addButtonPushHandler(buttonNum, new ButtonPushHandler() {
				@Override
				public void onButtonPush() {
					if(drinkIsBeingMaked) {
						return;
					} else {
						drinkIsBeingMaked = true;
						startDrinkMaking(finalButtonNum);
					}
				}
			});
			board.addButtonWorkFinishHandler(buttonNum, new WorkFinishHandler() {
				@Override
				public void onWorkFinish() {
					drinkIsBeingMaked = false;
					showDrinkIsComplete();
				}
			});
		}
	}

	private void startDrinkMaking(int buttonNum) {
		balanceLabel.setVisible(false);
		drinkReadyLabel.setVisible(false);
		drinkIsMakingLabel.setVisible(true);

		selectDrink(buttonNum);
		startDrinkMakingAnimation();
	}

	private void selectDrink(Integer drinkNum) {
		if(selectedDrink != null) {
			unselectDrink(selectedDrink);
		}
		if(drinkNum != null) {
			HBox panel = buttonPanelMap.get(drinkNum);
			panel.getStyleClass().remove("drink");
			panel.getStyleClass().add("drink-active");
		}
		selectedDrink = drinkNum;
	}

	private void unselectDrink(Integer drinkNum) {
		HBox panel = buttonPanelMap.get(drinkNum);
		panel.getStyleClass().remove("drink-active");
		panel.getStyleClass().add("drink");
	}

	private void startDrinkMakingAnimation() {
		drinkMakingAnimationTimer.schedule(new TimerTask() {

			private int i = 0;

			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						drinkIsMakingLabel.setVisible(i % 2 == 0);
						i++;
					}
				});
			}
		}, 0, DRINK_MAKING_BLINK_PERIOD);
	}

	private void showDrinkIsComplete() {
		stopDrinkMakingAnimation();
		balanceLabel.setVisible(false);
		drinkIsMakingLabel.setVisible(false);
		drinkReadyLabel.setVisible(true);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				drinkReadyLabel.setVisible(false);
				balanceLabel.setVisible(true);
			}
		}, DRINK_COMPLETE_SHOW_PERIOD);
	}

	private void stopDrinkMakingAnimation() {
		drinkMakingAnimationTimer.cancel();
	}

	private void readLabelsFromFile() {
		DrinkListManager drinkListManager = new DrinkListManager();
		List<Drink> drinks = drinkListManager.loadCurrentPricesAndTitles();

		for(int i = 0; i < drinks.size(); i++) {
			Drink drink = drinks.get(i);
			DrinkLabelPair labelPair = drinkLabelPairs.get(i);

			labelPair.setName(drink.getName());
			labelPair.setPrice(drink.getPrice());
		}
	}

	public void onMouse(MouseEvent mouseEvent) {
		if(mouseEvent.getClickCount() == 2) {
			SceneSwitcher.getInstance().switchToLoginWindow();
		}
	}

	private class DrinkLabelPair {
		private Label nameLabel;
		private Label priceLabel;

		public DrinkLabelPair(Label nameLabel, Label priceLabel) {
			this.nameLabel = nameLabel;
			this.priceLabel = priceLabel;
		}

		public void setName(String name) {
			nameLabel.setText(name);
		}

		public void setPrice(int price) {
			priceLabel.setText(price + " р");
		}
	}
}

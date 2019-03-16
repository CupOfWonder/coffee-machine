package com.parcel.coffee.controller;

import ITL_SCS_SPO.CURRENCY;
import ITL_SCS_SPO.SCS_SPO;
import ITL_SCS_SPO.SCS_SPO_event;
import ITL_SCS_SPO.SCS_SPO_event_listener;
import com.parcel.Board;
import com.parcel.coffee.SceneSwitcher;
import com.parcel.coffee.core.drinks.Drink;
import com.parcel.coffee.core.drinks.DrinkListManager;
import com.parcel.coffee.core.events.DrinkListChangeHandler;
import com.parcel.coffee.core.events.EventBus;
import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler;
import com.parcel.coffee.core.hardware.helpers.WorkFinishHandler;
import com.parcel.coffee.core.payment.Balance;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.*;

import static com.parcel.coffee.core.utils.CurrencyUtils.doubleToCurrency;

public class MainAppController implements SCS_SPO_event_listener {

	public Label name1, name2, name3, name4, name5, name6, price1, price2, price3, price4, price5, price6, balanceDigitLabel;
	public HBox drinkIsMakingLabel, drinkReadyLabel, balanceLabel;
	public HBox drinkPanel1, drinkPanel2, drinkPanel3, drinkPanel4, drinkPanel5, drinkPanel6;

	private List<DrinkLabelPair> drinkLabelPairs = new ArrayList<>();
	private Map<Integer, HBox> buttonPanelMap = new HashMap<>();

	private Board board = new Board();
	private SCS_SPO billAcceptor;

	private boolean drinkIsBeingMaked = false;

	private Timer drinkMakingAnimationTimer;

	private static final int DRINK_MAKING_BLINK_PERIOD = 700;
	private static final int DRINK_COMPLETE_SHOW_PERIOD = 1800;

	private Integer selectedDrinkNum;
	private Map<Integer, Drink> shownDrinkMap = new HashMap<>();

	private Balance balance = new Balance();

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
		//initBoard();
		initPaymentSystem();
	}

	private void initBoard() {
		if(!board.update()) {
			board.save();
		}
		board.generate();
		for(int buttonNum = 0; buttonNum < 6; buttonNum++) {

			int drinkNumber = buttonNum;
			board.setButtonPushHandler(buttonNum, new ButtonPushHandler() {
				@Override
				public void onButtonPush() {
					if(drinkIsBeingMaked) {
						return;
					} else {
						drinkIsBeingMaked = true;
						selectDrink(drinkNumber);
						tryStartToMakeSelectedDrink();
					}
				}
			});

			board.setButtonWorkFinishHandler(buttonNum, new WorkFinishHandler() {
				@Override
				public void onWorkFinish() {
					drinkIsBeingMaked = false;
					handleDrinkCompletion();
				}
			});
		}
	}

	private void initPaymentSystem() {
		billAcceptor = new SCS_SPO(null, this);
		refreshBalanceWidget();
	}

	private void tryStartToMakeSelectedDrink() {
		if(selectedDrinkNum != null) {
			Drink drink = shownDrinkMap.get(selectedDrinkNum);

			if(drink == null) {
				return;
			}

			int price = drink.getPrice();
			if(balance.checkHasEnoughForBuy(price)) {
				startDrinkMaking(selectedDrinkNum);
			}
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
		if(selectedDrinkNum != null) {
			unselectDrink(selectedDrinkNum);
		}
		if(drinkNum != null) {
			HBox panel = buttonPanelMap.get(drinkNum);
			panel.getStyleClass().remove("drink");
			panel.getStyleClass().add("drink-active");
		}
		selectedDrinkNum = drinkNum;
	}

	private void unselectDrink(Integer drinkNum) {
		HBox panel = buttonPanelMap.get(drinkNum);
		panel.getStyleClass().remove("drink-active");
		panel.getStyleClass().add("drink");
	}

	private void startDrinkMakingAnimation() {
		drinkMakingAnimationTimer = new Timer();
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

	private void handleDrinkCompletion() {
		giveCoinChange();

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
				selectDrink(null);
			}
		}, DRINK_COMPLETE_SHOW_PERIOD);
	}

	//Функция дает сдачу
	private void giveCoinChange() {
		billAcceptor.UnInhibitCoin(doubleToCurrency(balance.getBalance()));
		balance.reset();
	}

	private void stopDrinkMakingAnimation() {
		if(drinkMakingAnimationTimer != null) {
			drinkMakingAnimationTimer.cancel();
		}
	}

	private void readLabelsFromFile() {
		DrinkListManager drinkListManager = new DrinkListManager();
		List<Drink> drinks = drinkListManager.loadCurrentPricesAndTitles();

		for(int i = 0; i < drinks.size(); i++) {
			Drink drink = drinks.get(i);
			DrinkLabelPair labelPair = drinkLabelPairs.get(i);

			labelPair.setName(drink.getName());
			labelPair.setPrice(drink.getPrice());

			shownDrinkMap.put(i, drink);
		}
	}

	public void onMouse(MouseEvent mouseEvent) {
		if(mouseEvent.getClickCount() == 2) {
			SceneSwitcher.getInstance().switchToLoginWindow();
		}
	}

	@Override
	public void SCS_SPO_Event_Occurred(SCS_SPO_event scs_spo_event, CURRENCY currency) {
		switch (scs_spo_event) {
			case ev_NOTE_STACKED:
				int roubles = (int) currency.value;
				balance.addToBalance(roubles);
				refreshBalanceWidget();
				tryStartToMakeSelectedDrink();
		}
	}

	private void refreshBalanceWidget() {
		int roubles = balance.getBalance();
		balanceDigitLabel.setText(roubles+" р");
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

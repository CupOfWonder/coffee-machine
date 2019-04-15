package com.parcel.coffee.controller;

import com.parcel.Board;
import com.parcel.coffee.SceneSwitcher;
import com.parcel.coffee.core.drinks.Drink;
import com.parcel.coffee.core.drinks.DrinkListManager;
import com.parcel.coffee.core.events.DrinkListChangeHandler;
import com.parcel.coffee.core.events.EventBus;
import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler;
import com.parcel.coffee.core.hardware.helpers.WorkFinishHandler;
import com.parcel.coffee.core.payment.Balance;
import com.parcel.payment.parts.PaymentSystem;
import com.parcel.payment.parts.events.PaymentSystemEvent;
import com.parcel.payment.parts.events.PaymentSystemEventHandler;
import com.parcel.payment.parts.hardware.billacceptor.factory.BillAcceptorType;
import com.parcel.payment.parts.hardware.coinacceptor.factory.CoinAcceptorType;
import com.parcel.payment.parts.hardware.hopper.factory.HopperType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class MainAppController {

	private Logger logger = Logger.getLogger(MainAppController.class);

	@FXML
	public Label name1, name2, name3, name4, name5, name6, price1, price2, price3, price4, price5, price6,
			balanceDigitLabel, shortMessageLabel, blinkingMessageLabel;

	@FXML
	public HBox blinkingMessagePanel, shortMessagePanel, balanceLabel;

	@FXML
	public HBox drinkPanel1, drinkPanel2, drinkPanel3, drinkPanel4, drinkPanel5, drinkPanel6;

	private List<DrinkLabelPair> drinkLabelPairs = new ArrayList<>();
	private Map<Integer, HBox> buttonPanelMap = new HashMap<>();

	private Board board = new Board();

	private boolean drinkIsBeingMade = false;

	private Timer blinkingMessageTimer;

	private static final int DRINK_MAKING_BLINK_PERIOD = 700;
	private static final int DRINK_COMPLETE_SHOW_PERIOD = 1800;

	private Integer selectedDrinkNum;
	private Map<Integer, Drink> shownDrinkMap = new HashMap<>();

	private Balance balance = new Balance();
	private PaymentSystem paymentSystem = new PaymentSystem();

	private static final String DRINK_IS_MAKING_MSG = "Приготовление";
	private static final String DRINK_IS_READY_MSG = "Готово!";
	private static final String HOPPER_NO_MONEY_MSG = "Отсутствуют монеты для сдачи";

	@FXML
	public void initialize() {
		initUi();

		if(macAddressIsCorrect()) {
			initHardware();
		} else {
			showBlinkingMessage("Заплатите разработчикам");
		}
	}

	private boolean macAddressIsCorrect() {
		try {
			String rightMac = "b8:27:eb:8c:64:bb";
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				byte[] macBytes = ni.getHardwareAddress();
				String mac = byteArrToMac(macBytes);
				if(rightMac.equals(mac)) {
					return true;
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String byteArrToMac(byte[] macBytes) {
		if(macBytes == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder(18);
		for (byte b : macBytes) {
			if (sb.length() > 0)
				sb.append(':');
			sb.append(String.format("%02x", b));
		}
		return sb.toString().toLowerCase();
	}

	private boolean arraysAreEqual(byte[] macBytes, int[] rightMac) {
		if(macBytes == null || macBytes.length != rightMac.length) {
			return false;
		} else {
			for(int i = 0; i < macBytes.length; i++) {
				if(macBytes[i] != rightMac[i]) {
					return false;
				}
			}
			return true;
		}
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
		initBoard();
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
					if(drinkIsBeingMade) {
						return;
					} else {
						selectDrink(drinkNumber);
						tryStartToMakeSelectedDrink();
					}
				}
			});

			board.setButtonWorkFinishHandler(buttonNum, new WorkFinishHandler() {
				@Override
				public void onWorkFinish() {
					drinkIsBeingMade = false;
					handleDrinkCompletion();
				}
			});
		}
	}

	private void initPaymentSystem() {

		paymentSystem = new PaymentSystem();
		paymentSystem.setBillAcceptorType(BillAcceptorType.SSP_BILL_ACCEPTOR);
		paymentSystem.setHopperType(HopperType.SSP_HOPPER);
		paymentSystem.setCoinAcceptorType(CoinAcceptorType.IMPULSE);
		paymentSystem.init();

		paymentSystem.addEventHandler(new PaymentSystemEventHandler() {
			@Override
			public void onEvent(PaymentSystemEvent event) {
				switch (event.getType()) {
					case MONEY_INCOME:
						balance.addToBalance(event.getMoneyAmount());
						refreshBalanceWidget();
						tryStartToMakeSelectedDrink();
						break;
					case MONEY_DISPENSE_SUCCESS:
						break;
					case HOPPER_NO_MONEY:
					case HOPPER_NOT_EXACT_AMOUNT:
						showShortMessage(HOPPER_NO_MONEY_MSG);
						balance.reset();
						refreshBalanceWidget();

						break;


				}
			}
		});
		refreshBalanceWidget();
	}

	private void tryStartToMakeSelectedDrink() {
		System.out.println("Trying to start to make drink "+selectedDrinkNum);
		if(selectedDrinkNum != null) {
			Drink drink = shownDrinkMap.get(selectedDrinkNum);

			if(drink == null) {
				return;
			}

			int price = drink.getPrice();
			if(balance.checkHasEnoughForBuy(price)) {
				balance.substractFromBalance(price);
				//giveCoinsWithoutDrinkMaking(selectedDrinkNum);	//Команда для теста без платы для изготовления
				startDrinkMaking(selectedDrinkNum);
			}
		}

	}

	private void giveCoinsWithoutDrinkMaking(int buttonNum) {
		selectDrink(buttonNum);
		giveCoinChange();
		selectDrink(null);
	}

	private void startDrinkMaking(int buttonNum) {
		drinkIsBeingMade = true;
		selectDrink(buttonNum);
		showBlinkingMessage(DRINK_IS_MAKING_MSG);
		board.executeButtonScript(buttonNum);
	}

	private void selectDrink(Integer drinkNum) {
		
		if(selectedDrinkNum != null) {
			unselectDrink(selectedDrinkNum);
		}

		selectedDrinkNum = drinkNum;

		Platform.runLater(new Runnable() {
			
			public void run() {
				if(drinkNum != null) {
					HBox panel = buttonPanelMap.get(drinkNum);
					panel.getStyleClass().remove("drink");
					panel.getStyleClass().add("drink-active");
				}
			}
		});
	}

	private void unselectDrink(Integer drinkNum) {
		Platform.runLater(new Runnable() {
			public void run() {
				HBox panel = buttonPanelMap.get(drinkNum);
				panel.getStyleClass().remove("drink-active");
				panel.getStyleClass().add("drink");
			}
		});
	}

	private void showBlinkingMessage(String message) {
		System.out.println("Showing blinking message");
		Platform.runLater(new Runnable() {
			public void run() {

				balanceLabel.setVisible(false);
				shortMessagePanel.setVisible(false);
				blinkingMessagePanel.setVisible(true);

				blinkingMessageLabel.setText(message);
			}

		});
	
		blinkingMessageTimer = new Timer();

		blinkingMessageTimer.schedule(new TimerTask() {

			private boolean on = true;

			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						blinkingMessagePanel.setVisible(on);
						on = !on;
					}
				});
			}
		}, 0, DRINK_MAKING_BLINK_PERIOD);
	}

	private void handleDrinkCompletion() {
		giveCoinChange();
		stopBlinkingMessageAnimation();
		showShortMessage(DRINK_IS_READY_MSG);
		selectDrink(null);
	}

	private void showShortMessage(String message) {
		Platform.runLater(new Runnable() {
			public void run() {

				balanceLabel.setVisible(false);
				blinkingMessagePanel.setVisible(false);
				shortMessagePanel.setVisible(true);
				shortMessageLabel.setText(message);
			}
		});

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					public void run() {
						shortMessagePanel.setVisible(false);
						balanceLabel.setVisible(true);
					}
				});
			}
		}, DRINK_COMPLETE_SHOW_PERIOD);
	}

	//Функция дает сдачу
	private void giveCoinChange() {
		if(balance.getBalance() > 0) {
			paymentSystem.dispenseMoney(balance.getBalance());
			balance.reset();
			refreshBalanceWidget();
		}
	}

	private void stopBlinkingMessageAnimation() {
		if(blinkingMessageTimer != null) {
			blinkingMessageTimer.cancel();
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


	private void refreshBalanceWidget() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				int roubles = balance.getBalance();
				balanceDigitLabel.setText(roubles+" р");
			}
		});
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

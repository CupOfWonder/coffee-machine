package com.parcel.coffee.controller;

import com.parcel.Board;
import com.parcel.coffee.SceneSwitcher;
import com.parcel.coffee.core.commands.ComboCommand;
import com.parcel.coffee.core.commands.CommandExecutor;
import com.parcel.coffee.core.commands.SimpleCommand;
import com.parcel.coffee.core.commands.InterfaceCommand;
import com.parcel.coffee.core.drinks.Drink;
import com.parcel.coffee.core.drinks.DrinkListManager;
import com.parcel.coffee.core.events.DrinkListChangeHandler;
import com.parcel.coffee.core.events.EventBus;
import com.parcel.coffee.core.hardware.helpers.ButtonPushHandler;
import com.parcel.coffee.core.hardware.helpers.WorkFinishHandler;
import com.parcel.coffee.core.payment.CoinAmountRefresher;
import com.parcel.coffee.core.state.CoffeeMachineState;
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

	private Timer blinkingMessageTimer;

	private static final int DRINK_MAKING_BLINK_PERIOD = 700;
	private static final int DRINK_COMPLETE_SHOW_PERIOD = 1800;

	private Map<Integer, Drink> shownDrinkMap = new HashMap<>();

	private PaymentSystem paymentSystem = new PaymentSystem();
	private CoinAmountRefresher refresher;

	private static final String DRINK_IS_MAKING_MSG = "Приготовление";
	private static final String DRINK_IS_READY_MSG = "Готово!";
	private static final String HOPPER_NO_MONEY_MSG = "Отсутствуют монеты для сдачи";

	private CoffeeMachineState state = new CoffeeMachineState();
	private CommandExecutor commandExecutor = new CommandExecutor();

	@FXML
	public void initialize() {
		initUi();
		initExecutor();

		if(macAddressIsCorrect()) {
			initHardware();
		} else {
			showBlinkingMessage("Заплатите разработчикам");
		}
	}

	private boolean macAddressIsCorrect() {
		try {
//			String rightMac = "b8:27:eb:8c:64:bb"; //Клиента
			String rightMac = "50:3e:aa:4a:c5:5f"; //Мой
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

	private void initExecutor() {
		commandExecutor.run();
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
					if(state.checkBusy()) {
						return;
					} else {
						commandExecutor.addCommandToQueue(new SelectDrinkCommand(drinkNumber));
						commandExecutor.addCommandToQueue(new TryToStartMakeDrinkCommand());
					}
				}
			});

			board.setButtonWorkFinishHandler(buttonNum, new WorkFinishHandler() {
				@Override
				public void onWorkFinish() {
					state.setBusy(false);
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
						commandExecutor.addCommandToQueue(new AddToBalanceCommand(event.getMoneyAmount()));
						commandExecutor.addCommandToQueue(new TryToStartMakeDrinkCommand());
						break;
					case MONEY_DISPENSE_SUCCESS:
						break;
					case HOPPER_NO_MONEY:
					case HOPPER_NOT_EXACT_AMOUNT:
						commandExecutor.addCommandToQueue(new ShowShortMessageCommand(HOPPER_NO_MONEY_MSG));
						commandExecutor.addCommandToQueue(new RefreshBalanceCommand());
						break;


				}
			}
		});
		commandExecutor.addCommandToQueue(new RefreshBalanceCommand());
		launchCoinAmountRefresher(paymentSystem);
	}

	private class AddToBalanceCommand extends SimpleCommand {

		private final int amount;

		public AddToBalanceCommand(int amount) {
			this.amount = amount;
		}

		@Override
		public void execute() {
			state.addToBalance(amount);
			commandExecutor.addCommandToQueue(new RefreshBalanceCommand());
		}
	}

	private void launchCoinAmountRefresher(PaymentSystem paymentSystem) {
		refresher = new CoinAmountRefresher();
		refresher.launchRefresh(paymentSystem, commandExecutor);
	}

	private class TryToStartMakeDrinkCommand extends SimpleCommand {

		@Override
		public void execute() {
			System.out.println("Trying to start to make drink "+state.getSelectedDrink());

			Integer selectedDrink = state.getSelectedDrink();
			if(selectedDrink != null) {
				Drink drink = shownDrinkMap.get(selectedDrink);

				if(drink == null) {
					return;
				}

				int price = drink.getPrice();
				if(state.checkHasEnoughForBuy(price)) {
					state.substractFromBalance(price);
					state.rememberValueForChange(state.getBalance());
					startDrinkMaking(selectedDrink);
				}
			}
		}

		private void startDrinkMaking(int buttonNum) {
			state.setBusy(true);
			showBlinkingMessage(DRINK_IS_MAKING_MSG);
			board.executeButtonScript(buttonNum);
		}
	}

	private class SelectDrinkCommand extends ComboCommand {

		private final int drinkNum;

		public SelectDrinkCommand(int drinkNum) {
			this.drinkNum = drinkNum;
		}

		@Override
		public void doSimply() {
			if(state.getSelectedDrink() != null) {
				commandExecutor.addCommandToQueue(new UnselectDrinkCommand(drinkNum));
			}
			state.drinkWasSelected(drinkNum);
		}

		@Override
		public void doInInterface() {

			HBox panel = buttonPanelMap.get(drinkNum);
			panel.getStyleClass().remove("drink");
			panel.getStyleClass().add("drink-active");
		}
	}

	private class UnselectDrinkCommand extends InterfaceCommand {

		private final int drinkNum;

		public UnselectDrinkCommand(int drinkNum) {
			this.drinkNum = drinkNum;
		}

		@Override
		public void doInInterface() {
			doUnselectOnInterface(drinkNum);
		}
	}

	private class ResetSelectionCommand extends ComboCommand {

		private Integer drinkToUnselect;

		@Override
		public void doSimply() {
			this.drinkToUnselect = state.getSelectedDrink();
			state.resetSelection();
		}

		@Override
		public void doInInterface() {
			doUnselectOnInterface(drinkToUnselect);
		}
	}

	private void doUnselectOnInterface(int drinkNum) {
		HBox panel = buttonPanelMap.get(drinkNum);
		panel.getStyleClass().remove("drink-active");
		panel.getStyleClass().add("drink");
	}

	private class ShowBlinkingMessageCommand extends InterfaceCommand{

		private final String message;

		public ShowBlinkingMessageCommand(String message) {
			this.message = message;
		}

		@Override
		public void doInInterface() {
			balanceLabel.setVisible(false);
			shortMessagePanel.setVisible(false);
			blinkingMessagePanel.setVisible(true);
			blinkingMessageLabel.setText(message);

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
	}

	private void showBlinkingMessage(String message) {
		commandExecutor.addCommandToQueue(new ShowBlinkingMessageCommand(message));
	}

	private void handleDrinkCompletion() {
		stopBlinkingMessageAnimationIfNeeded();
		
		commandExecutor.addCommandToQueue(new GiveCoinChangeCommand());
		commandExecutor.addCommandToQueue(new ShowShortMessageCommand(DRINK_IS_READY_MSG));
		commandExecutor.addCommandToQueue(new UnselectDrinkCommand(state.getSelectedDrink()));
	}

	private class ShowShortMessageCommand extends InterfaceCommand {

		private final String message;

		public ShowShortMessageCommand(String message) {
			this.message = message;
		}

		@Override
		public void doInInterface() {
			stopBlinkingMessageAnimationIfNeeded();

			balanceLabel.setVisible(false);
			blinkingMessagePanel.setVisible(false);
			shortMessagePanel.setVisible(true);
			shortMessageLabel.setText(message);

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
	}

	private class GiveCoinChangeCommand extends SimpleCommand {

		@Override
		public void execute() {
			if(state.getValueForChange() > 0) {
				paymentSystem.dispenseMoney(state.getValueForChange());
				state.substractChangeFromBalance();
				commandExecutor.addCommandToQueue(new RefreshBalanceCommand());
			}
		}
	}

	private void stopBlinkingMessageAnimationIfNeeded() {
		if(blinkingMessageTimer != null) {
			blinkingMessageTimer.cancel();
			blinkingMessageTimer = null;
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
			stopBlinkingMessageAnimationIfNeeded();
			SceneSwitcher.getInstance().switchToLoginWindow();
		}
	}

	private class RefreshBalanceCommand extends InterfaceCommand {
		@Override
		public void doInInterface() {
			int roubles = state.getBalance();
			balanceDigitLabel.setText(roubles+" р");
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

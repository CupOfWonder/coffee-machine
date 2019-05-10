package com.parcel.coffee.controller;

import com.parcel.Board;
import com.parcel.coffee.SceneSwitcher;
import com.parcel.coffee.core.commands.ComboCommand;
import com.parcel.coffee.core.commands.CommandExecutor;
import com.parcel.coffee.core.commands.InterfaceCommand;
import com.parcel.coffee.core.commands.SimpleCommand;
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
import com.parcel.payment.parts.utils.ThreadUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
	public HBox blinkingMessagePanel, shortMessagePanel, balancePanel;

	@FXML
	public HBox drinkPanel1, drinkPanel2, drinkPanel3, drinkPanel4, drinkPanel5, drinkPanel6;

	private List<DrinkLabelPair> drinkLabelPairs = new ArrayList<>();
	private Map<Integer, HBox> buttonPanelMap = new HashMap<>();

	private Board board = new Board();

	private static final int DRINK_MAKING_BLINK_PERIOD = 700;
	private static final int DRINK_COMPLETE_SHOW_PERIOD = 1800;

	private Map<Integer, Drink> shownDrinkMap = new HashMap<>();

	private PaymentSystem paymentSystem = new PaymentSystem();
	private CoinAmountRefresher refresher;

	private static final String DRINK_IS_MAKING_MSG = "Приготовление";
	private static final String DRINK_IS_READY_MSG = "Готово!";
	private static final String HOPPER_NO_MONEY_MSG = "Нет монет для сдачи";
	private static final String EQUIPMENT_ERROR_MSG = "Ошибка оборудования";
	private static final String RETURNING_TO_WORK = "Возвращение к работе";

	private CoffeeMachineState state = new CoffeeMachineState();
	private CommandExecutor commandExecutor = new CommandExecutor();

	private TopScreenWidgetController topScreenWidgetController;

	@FXML
	public void initialize() {
		initUi();
		initExecutor();

		if(!macAddressIsCorrect()) {
			initHardware();
		} else {
			addBlinkMessageToQueue("Заплатите разработчикам");
		}
	}

	private boolean macAddressIsCorrect() {
		try {
			String rightMac = "b8:27:eb:8c:64:bb"; //Клиента
//			String rightMac = "50:3e:aa:4a:c5:5f"; //Мой
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
		topScreenWidgetController = new TopScreenWidgetController();

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
					commandExecutor.addCommandToQueue(new SelectDrinkCommand(drinkNumber));
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
						break;

					case HOPPER_DISCONNECTED:
					case BILL_ACCEPTOR_DISCONNECTED:
						runReconnection();
						break;

				}
			}
		});
		commandExecutor.addCommandToQueue(new RefreshBalanceCommand());
		launchCoinAmountRefresher(paymentSystem);
	}

	private void runReconnection() {
		DeviceReconnector reconnector = new DeviceReconnector();
		reconnector.reconnectAllDevices();
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
					
					state.setBusy(true);
					addBlinkMessageToQueue(DRINK_IS_MAKING_MSG);
					commandExecutor.addCommandToQueue(new StartDrinkMakingCommand(selectedDrink));
				}
			}
		}

	}

	private class StartDrinkMakingCommand extends SimpleCommand {
		
		private final int drinkNum;

		public StartDrinkMakingCommand(int drinkNum) {
			this.drinkNum = drinkNum;
		}

		public void execute() {
			board.executeButtonScript(drinkNum);
		}
	}

	private class SelectDrinkCommand extends ComboCommand {

		private final int drinkNum;
		private final Integer oldDrinkNum;

		public SelectDrinkCommand(int drinkNum) {
			this.drinkNum = drinkNum;
			this.oldDrinkNum = state.getSelectedDrink();
		}

		@Override
		protected boolean canDoCommand() {
			return !state.checkBusy() && state.getBalance() > 0;
		}

		@Override
		public void doSimply() {
			state.drinkWasSelected(drinkNum);
			commandExecutor.addCommandToQueue(new TryToStartMakeDrinkCommand());
		}

		@Override
		public void doInInterface() {
			doUnselectOnInterface();
			HBox panel = buttonPanelMap.get(drinkNum);
			panel.getStyleClass().remove("drink");
			panel.getStyleClass().add("drink-active");
		}
	}

	private class ResetSelectionCommand extends ComboCommand {

		@Override
		public void doSimply() {
			state.resetSelection();
		}

		@Override
		public void doInInterface() {
			doUnselectOnInterface();
		}
	}

	private void doUnselectOnInterface() {
		for(int drink = 0; drink < 6; drink++) {
			HBox panel = buttonPanelMap.get(drink);
			panel.getStyleClass().remove("drink-active");
			panel.getStyleClass().add("drink");
		}
	}

	private class ShowBlinkingMessageCommand extends InterfaceCommand{

		private final String message;

		public ShowBlinkingMessageCommand(String message) {
			this.message = message;
		}

		@Override
		public void doInInterface() {
			topScreenWidgetController.showBlinkingMessage(message);
		}
	}

	private void addBlinkMessageToQueue(String message) {
		commandExecutor.addCommandToQueue(new ShowBlinkingMessageCommand(message));
	}

	private void handleDrinkCompletion() {
		commandExecutor.addCommandToQueue(new ShowShortMessageCommand(DRINK_IS_READY_MSG));
		commandExecutor.addCommandToQueue(new GiveCoinChangeCommand());
		commandExecutor.addCommandToQueue(new ResetSelectionCommand());
	}

	private class ShowShortMessageCommand extends InterfaceCommand {

		private final String message;

		public ShowShortMessageCommand(String message) {
			this.message = message;
		}

		@Override
		public void doInInterface() {
			topScreenWidgetController.showShortMessage(message);
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
			topScreenWidgetController.stopAllTimers();
			SceneSwitcher.getInstance().switchToLoginWindow();
		}
	}

	private class RefreshBalanceCommand extends InterfaceCommand {
		@Override
		public void doInInterface() {
			topScreenWidgetController.refreshBalance();
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

	private class TopScreenWidgetController {
		private volatile TopScreenState screenState;

		private Timer shortMessageTimer;
		private Timer blinkingMessageTimer;

		public void showAndRefreshBalance() {
			screenState = TopScreenState.BALANCE;

			showTopScreenWidget(balancePanel);
			refreshBalance();
		}

		public void refreshBalance() {
			int roubles = state.getBalance();
			balanceDigitLabel.setText(roubles+" р");
		}

		public void showBlinkingMessage(String message) {
			screenState = TopScreenState.BLINKING_MESSAGE;

			hideAllTopScreenWidgets();

			blinkingMessageLabel.setText(message);

			blinkingMessageTimer = new Timer();
			blinkingMessageTimer.schedule(new TimerTask() {

				private boolean on = true;

				@Override
				public void run() {
					if(screenState != TopScreenState.BLINKING_MESSAGE) {
						blinkingMessageTimer.cancel();
						blinkingMessageTimer = null;
						return;
					}

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

		public void showShortMessage(String message) {
			screenState = TopScreenState.SHORT_MESSAGE;

			shortMessageLabel.setText(message);
			showTopScreenWidget(shortMessagePanel);

			shortMessageTimer = new Timer();
			shortMessageTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					Platform.runLater(new Runnable() {
						public void run() {
							showAndRefreshBalance();
						}
					});
				}
			}, DRINK_COMPLETE_SHOW_PERIOD);
		}

		private void hideAllTopScreenWidgets() {
			balancePanel.setVisible(false);
			blinkingMessagePanel.setVisible(false);
			shortMessagePanel.setVisible(false);
		
		}

		private void showTopScreenWidget(Node topScreenWidget) {
			hideAllTopScreenWidgets();
			topScreenWidget.setVisible(true);
		}

		public void stopAllTimers() {
			if(blinkingMessageTimer != null) {
				blinkingMessageTimer.cancel();
				blinkingMessageTimer = null;
			}

			if(shortMessageTimer != null) {
				shortMessageTimer.cancel();
				shortMessageTimer = null;
			}

		}
	}

	private enum TopScreenState {
		BALANCE,
		BLINKING_MESSAGE,
		SHORT_MESSAGE
	}

	private class DeviceReconnector {

		private static final int DEFAULT_WAIT_PERIOD = 5000;

		public void reconnectAllDevices() {
			logger.info("Starting device reconnection");

			ShowBlinkingMessageCommand command = new ShowBlinkingMessageCommand(EQUIPMENT_ERROR_MSG);
			command.setShutdownAfterCommand(true);
			commandExecutor.addCommandToQueue(command);

			startReconnection();
		}

		private void startReconnection() {
			paymentSystem.disableBillAcception();

			do {
				while (paymentSystem.hopperIsDisconnected()) {
					ThreadUtils.sleep(DEFAULT_WAIT_PERIOD);
					paymentSystem.reconnectHopper();
				}

				while (paymentSystem.billAcceptorIsDisconnected()) {
					ThreadUtils.sleep(DEFAULT_WAIT_PERIOD);
					paymentSystem.reconnectBillAcceptor();
				}

			} while (!paymentSystem.allPresentDevicesAreConnected());

			logger.info("Successfully reconnected!");
			paymentSystem.enableBillAcception();

			commandExecutor.addCommandToQueue(new ShowShortMessageCommand(RETURNING_TO_WORK));
			commandExecutor.run();
		}
	}

}

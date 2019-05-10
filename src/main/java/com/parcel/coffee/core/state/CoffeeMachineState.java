package com.parcel.coffee.core.state;

public class CoffeeMachineState {
	private boolean isBusy;
	private boolean isReconnecting;
	private Integer selectedDrink;

	private int balance = 0;
	private int valueForChange;

	public synchronized boolean somethingIsSelected() {
		return selectedDrink != null;
	}

	public synchronized Integer getSelectedDrink() {
		return selectedDrink;
	}

	public synchronized void drinkWasSelected(int selectedDrink) {
		this.selectedDrink = selectedDrink;
	}

	public synchronized void unselectDrink() {
		selectedDrink = null;
	}

	public synchronized boolean checkBusy() {
		return isBusy;
	}

	public synchronized void setBusy(boolean busy) {
		this.isBusy = busy;
	}

	public synchronized void addToBalance(int valueToAdd) {
		balance += valueToAdd;
	}

	public synchronized boolean checkHasEnoughForBuy(int price) {
		return balance >= price;
	}

	public synchronized void substractFromBalance(int price) {
		balance -= price;
	}

	public synchronized void resetSelection() {
		selectedDrink = null;
	}

	public synchronized int getBalance() {
		return balance;
	}

	public synchronized void rememberValueForChange(int value) {
		this.valueForChange = value;
	}

	public synchronized void substractChangeFromBalance() {
		balance -= valueForChange;
		valueForChange = 0;
	}

	public synchronized int getValueForChange() {
		return valueForChange;
	}

	public synchronized boolean isReconnecting() {
		return isReconnecting;
	}

	public void setReconnecting(boolean reconnecting) {
		isReconnecting = reconnecting;
	}
}

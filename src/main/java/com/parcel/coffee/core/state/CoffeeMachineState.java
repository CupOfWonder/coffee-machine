package com.parcel.coffee.core.state;

public class CoffeeMachineState {
	private boolean isBusy;
	private Integer selectedDrink;

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
}

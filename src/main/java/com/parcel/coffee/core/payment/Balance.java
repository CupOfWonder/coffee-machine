package com.parcel.coffee.core.payment;

public class Balance {
	private int value = 0;

	public void addToBalance(int valueToAdd) {
		value += valueToAdd;
	}

	public boolean checkHasEnoughForBuy(int price) {
		return value >= price;
	}

	public void substractFromBalance(int price) {
		value -= price;
	}

	public void reset() {
		value = 0;
	}

	public int getBalance() {
		return value;
	}
}

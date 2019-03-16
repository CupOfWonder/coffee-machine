package com.parcel.coffee.core.utils;

import ITL_SCS_SPO.CURRENCY;

public class CurrencyUtils {
	public static CURRENCY doubleToCurrency(double value) {
		CURRENCY currency = new CURRENCY();
		currency.value = value;
		currency.countryCode = "7";

		return currency;
	}
}

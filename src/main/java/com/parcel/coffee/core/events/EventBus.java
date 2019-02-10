package com.parcel.coffee.core.events;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

	private List<DrinkListChangeHandler> drinkListChangeHandlers = new ArrayList<>();

	private static EventBus instance;

	private EventBus() {
	}

	public static EventBus getInstance() {
		if(instance == null) {
			instance = new EventBus();
		}
		return instance;
	}

	public void fireDrinkListChanged() {
		for(DrinkListChangeHandler handler : drinkListChangeHandlers) {
			handler.onDrinkListChanged();
		}
	}

	public void addDrinkListChangeHandler(DrinkListChangeHandler handler) {
		drinkListChangeHandlers.add(handler);
	}
}

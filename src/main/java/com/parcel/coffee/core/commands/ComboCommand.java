package com.parcel.coffee.core.commands;

import javafx.application.Platform;

public abstract class ComboCommand extends Command {
	@Override
	public void execute() {
		doSimply();
		Platform.runLater(this::doInInterface);
	}

	public abstract void doSimply();

	public abstract void doInInterface();

}

package com.parcel.coffee.core.commands;

import javafx.application.Platform;

public abstract class InterfaceCommand extends Command {

	public abstract void doInInterface();

	@Override
	public void execute() {
		Platform.runLater(this::doInInterface);
	}
}

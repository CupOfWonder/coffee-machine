package com.parcel.coffee.core.commands;

public abstract class Command {
	public final void executeIfPossible() {
		if(canDoCommand()) {
			execute();
		}
	}

	protected abstract void execute();

	protected boolean canDoCommand() {
		return true;
	}

}

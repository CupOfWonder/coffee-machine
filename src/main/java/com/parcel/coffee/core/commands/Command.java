package com.parcel.coffee.core.commands;

public abstract class Command {

	private boolean shutdownAfterCommand = false;

	public final void executeIfPossible() {
		if(canDoCommand()) {
			execute();
		}
	}

	protected abstract void execute();

	protected boolean canDoCommand() {
		return true;
	}


	public boolean needShutdownAfterCommand() {
		return shutdownAfterCommand;
	}

	public void setShutdownAfterCommand(boolean shutdownAfterCommand) {
		this.shutdownAfterCommand = shutdownAfterCommand;
	}
}

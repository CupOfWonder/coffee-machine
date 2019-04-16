package com.parcel.coffee.core.commands;

public abstract class InterfaceCommand extends Command {
	@Override
	public CommandType getType() {
		return CommandType.INTERFACE;
	}
}

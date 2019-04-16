package com.parcel.coffee.core.commands;

public abstract class HardwareCommand extends Command{

	@Override
	public CommandType getType() {
		return CommandType.HARDWARE;
	}
}

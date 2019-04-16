package com.parcel.coffee.core.commands;

public abstract class Command {
	public abstract void execute();

	public abstract CommandType getType();
}

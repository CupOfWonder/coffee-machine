package com.parcel.coffee.core.commands;

import com.parcel.payment.parts.utils.ThreadUtils;
import javafx.application.Platform;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CommandExecutor {
	private Thread commandThread;

	private BlockingQueue<Command> queue = new ArrayBlockingQueue<Command>(1000);

	private volatile boolean shutdown;
	private static final Object monitor = new Object();

	public void run() {
		commandThread = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (monitor) {
					do {
						runCommandsWhilePossible();

						ThreadUtils.wait(monitor);

					} while (!shutdown);
				}
			}

			private void runCommandsWhilePossible() {
				while(!queue.isEmpty()) {
					Command command = queue.poll();

					switch (command.getType()) {
						case HARDWARE:
							command.execute();
							break;
						case INTERFACE:
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									command.execute();
								}
							});
							break;
					}
				}
			}
		});
		commandThread.start();
	}

	public void addCommandToQueue(Command command) {
		synchronized (monitor) {
			queue.add(command);
			monitor.notifyAll();
		}
	}

	public void shutdown() {
		shutdown = true;
		monitor.notifyAll();
	}

}

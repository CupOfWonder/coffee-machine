package com.parcel.coffee.core.commands;

import com.parcel.payment.parts.utils.ThreadUtils;

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
						System.out.println("Iteration is running");
						runCommandsWhilePossible();

						ThreadUtils.wait(monitor);

					} while (!shutdown);
				}
			}

			private void runCommandsWhilePossible() {
				while(!queue.isEmpty()) {
					Command command = queue.poll();
					System.out.println("Running now: "+command.getClass().getSimpleName());
					command.execute();
				}
			}
		});
		commandThread.start();
	}

	public void addCommandToQueue(Command command) {
		System.out.println("Added to queue: "+command.getClass().getSimpleName());
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

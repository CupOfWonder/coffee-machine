package com.parcel.coffee.core.commands;

import com.parcel.payment.parts.utils.ThreadUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CommandExecutor {

	private Logger logger = Logger.getLogger(CommandExecutor.class);

	private Thread commandThread;

	private BlockingQueue<Command> queue = new ArrayBlockingQueue<Command>(1000);

	private volatile boolean shutdown;
	private static final Object monitor = new Object();

	public void run() {
		shutdown = false;

		commandThread = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (monitor) {
					do {
						runCommandsWhilePossible();

						ThreadUtils.wait(monitor);

					} while (!shutdown);

					logger.info("Shutting down command executor");
				}
			}

			private void runCommandsWhilePossible() {
				while(!queue.isEmpty()) {
					Command command = queue.poll();
					logger.info("Running now: "+command.getClass().getSimpleName());
					command.executeIfPossible();

					if(command.needShutdownAfterCommand()) {
						shutdown = true;
						break;
					}
				}
			}
		});
		commandThread.start();
	}

	public void addCommandToQueue(Command command) {
		logger.info("Added to queue: "+command.getClass().getSimpleName());
		synchronized (monitor) {
			queue.add(command);
			monitor.notifyAll();
		}
	}

	public void shutdown() {
		synchronized (monitor) {
			shutdown = true;
			monitor.notifyAll();
		}
	}

}

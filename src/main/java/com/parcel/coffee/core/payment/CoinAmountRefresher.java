package com.parcel.coffee.core.payment;

import com.parcel.coffee.core.commands.CommandExecutor;
import com.parcel.coffee.core.commands.SimpleCommand;
import com.parcel.payment.parts.PaymentSystem;
import com.parcel.payment.parts.utils.ThreadUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class CoinAmountRefresher {

	private Logger logger = Logger.getLogger(CoinAmountRefresher.class);

	public void launchRefresh(PaymentSystem system, CommandExecutor executor) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				List<Integer> coinValues = Arrays.asList(1, 2, 5, 10);

				for(Integer value : coinValues) {
					executor.addCommandToQueue(new SimpleCommand() {
						@Override
						public void execute() {
							system.setHopperCoinAmount(10_000, value);
							ThreadUtils.sleep(400);
						}
					});
				}
			}
		}, todayMidnight(), 24*60*60*1000);
	}

	private Date todayMidnight() {
		Calendar calendar =  Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		return calendar.getTime();
	}
}

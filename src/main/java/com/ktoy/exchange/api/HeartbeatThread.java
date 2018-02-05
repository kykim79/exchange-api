/*******************************************************************************
 *
 *    Copyright (C) 2018 Kim Kwon Young
 *  
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License. 
 *    
 *******************************************************************************/
package com.ktoy.exchange.api;

import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import com.ktoy.exchange.api.manager.QuoteManager;
import com.ktoy.exchange.api.util.EventsInTimeslotManager;
import org.bboxdb.commons.concurrent.ExceptionSafeThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HeartbeatThread extends ExceptionSafeThread {

	/**
	 * The ticker timeout
	 */
	private static final long TICKER_TIMEOUT = TimeUnit.MINUTES.toMillis(5);
	
	/**
	 * The API timeout
	 */
	private static final long CPNNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(45);
	
	/**
	 * The API timeout
	 */
	private static final long HEARTBEAT = TimeUnit.SECONDS.toMillis(5);
	
	/**
	 * Max reconnects in 10 minutes
	 */
	private static final int MAX_RECONNECTS_IN_TIME = 10;

	/**
	 * The API broker
	 */
	private final ApiBroker apiBroker;

	/**
	 * THe API command
	 */
	private final AbstractAPICommand apiCommand;

	/**
	 * The reconnect timeslot manager
	 */
	private final EventsInTimeslotManager eventsInTimeslotManager;
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(HeartbeatThread.class);

	
	/**
	 * @param apiBroker
	 */
	public HeartbeatThread(final ApiBroker apiBroker, final AbstractAPICommand apiCommand) {
		this.apiBroker = apiBroker;

		this.apiCommand = apiCommand;
		
		this.eventsInTimeslotManager = new EventsInTimeslotManager(
				MAX_RECONNECTS_IN_TIME, 
				10, 
				TimeUnit.MINUTES);
	}

	@Override
	public void runThread() {
		
		try {
			while(! Thread.interrupted()) {
				
				Thread.sleep(TimeUnit.SECONDS.toMillis(3));
				
				final WebsocketClientEndpoint websocketEndpoint = apiBroker.getWebsocketEndpoint();
				
				if(websocketEndpoint == null) {
					continue;
				}
					
				if(! websocketEndpoint.isConnected()) {
					logger.error("We are not connected, reconnecting");
					executeReconnect();
					continue;
				}
				
				sendHeartbeatIfNeeded();

				final boolean tickerUpToDate = checkTickerFreshness();
				
				if(! tickerUpToDate) {
					logger.error("Ticker are outdated, reconnecting");
					executeReconnect();
					continue;
				}
				
				final boolean reconnectNeeded = checkConnectionTimeout();
				
				if(reconnectNeeded) {
					logger.error("Global connection heartbeat time out, reconnecting");
					executeReconnect();
					continue;
				}
			}
		} catch (InterruptedException e) {
			logger.debug("Heartbeat thread was interrupted, exiting");
			Thread.currentThread().interrupt();
			return;
		}
	}

	/**
	 * Are all ticker uptodate
	 * @return
	 */
	private boolean checkTickerFreshness() {
		
		final long currentTime = System.currentTimeMillis();
		final QuoteManager tickerManager = apiBroker.getQuoteManager();
		final Set<ChannelSymbol> activeSymbols = tickerManager.getActiveSymbols();
		
		for(final ChannelSymbol symbol : activeSymbols) {
			final long lastHeatbeat = tickerManager.getHeartbeatForSymbol(symbol);
			
			if(lastHeatbeat == -1) {
				continue;
			}
						
			if(lastHeatbeat + TICKER_TIMEOUT < currentTime) {
				logger.error("Last update for symbol {} is {} current time is {}, the data is outdated",
						symbol, lastHeatbeat, currentTime);
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Send a heartbeat package on the connection
	 */
	private void sendHeartbeatIfNeeded() {
		final long nextHeartbeat = apiBroker.getLastHeatbeat().get() + HEARTBEAT;

		if(nextHeartbeat < System.currentTimeMillis()) {
			logger.debug("Send heartbeat");
			apiBroker.sendCommand(apiCommand);
		}
	}

	/**
	 * Check for connection timeout
	 * @return 
	 */
	private boolean checkConnectionTimeout() {
		final long heartbeatTimeout = apiBroker.getLastHeatbeat().get() + CPNNECTION_TIMEOUT;
		
		if(heartbeatTimeout < System.currentTimeMillis()) {
			logger.error("Heartbeat timeout reconnecting");
			return true;
		}
		
		return false;
	}

	/**
	 * Execute the reconnect
	 * @throws InterruptedException 
	 */
	private void executeReconnect() throws InterruptedException {
		// Close connection
		apiBroker.getWebsocketEndpoint().close();
		
		// Store the reconnect time to prevent to much
		// reconnects in a short timeframe. Otherwise the
		// rate limit will apply and the reconnects are not successfully
		logger.info("Wait for next reconnect timeslot");
		eventsInTimeslotManager.recordNewEvent();
		eventsInTimeslotManager.waitForNewTimeslot();
		logger.info("Wait for next reconnect timeslot DONE");

		apiBroker.reconnect();
	}
}
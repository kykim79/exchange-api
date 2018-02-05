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
package com.ktoy.exchange.api.manager;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import org.ta4j.core.Tick;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

public class QuoteManager {
	
	/**
	 * The last ticks
	 */
	protected final Map<ChannelSymbol, Tick> lastTick;
	
	/**
	 * The last tick timestamp
	 */
	protected final Map<ChannelSymbol, Long> lastTickTimestamp;
	
	/**
	 * The CurrencyPair callbacks
	 */
	private final BiConsumerCallbackManager<ChannelSymbol, Tick> tickerCallbacks;

	/**
	 * The Candlestick callbacks
	 */
	private final BiConsumerCallbackManager<ChannelSymbol, Tick> candleCallbacks;
	
	/**
	 * The executor service
	 */
	private final ExecutorService executorService;

	/**
	 * The API
	 */
	private final ApiBroker apiBroker;

	public QuoteManager(final ApiBroker apiBroker) {
		this.apiBroker = apiBroker;
		this.executorService = apiBroker.getExecutorService();
		this.lastTick = new HashMap<>();
		this.lastTickTimestamp = new HashMap<>();
		this.tickerCallbacks = new BiConsumerCallbackManager<>(executorService);
		this.candleCallbacks = new BiConsumerCallbackManager<>(executorService);
	}
	
	/**
	 * Get the last heartbeat for the symbol
	 * @param symbol
	 * @return
	 */
	public long getHeartbeatForSymbol(final ChannelSymbol symbol) {
		synchronized (lastTick) {
			final Long heartbeat = lastTickTimestamp.get(symbol);
			
			if(heartbeat == null) {
				return -1;
			}
			
			return heartbeat;
		}
	}
	
	/**
	 * Update the channel heartbeat
	 * @param symbol
	 */
	public void updateChannelHeartbeat(final ChannelSymbol symbol) {
		synchronized (lastTick) {
			lastTickTimestamp.put(symbol, System.currentTimeMillis());
		}
	}
	
	/**
	 * Get a set with active symbols
	 * @return
	 */
	public Set<ChannelSymbol> getActiveSymbols() {
		synchronized (lastTick) {
			return lastTick.keySet();
		}
	}
	
	/**
	 * Get the last tick for a given symbol
	 * @param currencyPair
	 * @return 
	 */
	public Tick getLastTick(final ChannelSymbol currencyPair) {
		synchronized (lastTick) {
			return lastTick.get(currencyPair);
		}
	}
	
	/**
	 * Invalidate the ticket heartbeat values
	 */
	public void invalidateTickerHeartbeat() {
		// Invalidate last tick timetamps
		synchronized (lastTick) {
			lastTickTimestamp.clear();	
		}
	}
	
	/**
	 * Register a new tick callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerTickCallback(final ChannelSymbol symbol,
			final BiConsumer<ChannelSymbol, Tick> callback) throws APIException {
		
		tickerCallbacks.registerCallback(symbol, callback);
	}
	
	/**
	 * Remove the a tick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeTickCallback(final ChannelSymbol symbol,
			final BiConsumer<ChannelSymbol, Tick> callback) throws APIException {
		
		return tickerCallbacks.removeCallback(symbol, callback);
	}
	
	/**
	 * Process a list with ticks
	 * @param symbol
	 * @param ticksBuffer
	 */
	public void handleTicksList(final ChannelSymbol symbol, final List<Tick> ticksBuffer) {
		tickerCallbacks.handleEventsList(symbol, ticksBuffer);
	}
	
	/**
	 * Handle a new tick
	 * @param currencyPair
	 * @param tick
	 */
	public void handleNewTick(final ChannelSymbol currencyPair, final Tick tick) {
		
		synchronized (lastTick) {
			lastTick.put(currencyPair, tick);
			lastTickTimestamp.put(currencyPair, System.currentTimeMillis());
		}
		
		tickerCallbacks.handleEvent(currencyPair, tick);
	}

	/**
	 * Subscribe a ticker
	 * @param command
	 */
	public void subscribeTicker(AbstractAPICommand command) {
		apiBroker.sendCommand(command);
	}
	
	/**
	 * Unsubscribe a ticker
	 * @param apiCommand
	 * @param currencyPair
	 */
	public void unsubscribeTicker(AbstractAPICommand apiCommand, final ChannelSymbol currencyPair) {
		final int channel = apiBroker.getChannelForSymbol(currencyPair);
		
		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + currencyPair);
		}

		apiBroker.sendCommand(apiCommand);
		apiBroker.removeChannelForSymbol(currencyPair);
	}
	
	/**
	 * Register a new candlestick callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerCandlestickCallback(final ChannelSymbol symbol,
			final BiConsumer<ChannelSymbol, Tick> callback) throws APIException {
		
		candleCallbacks.registerCallback(symbol, callback);
	}
	
	/**
	 * Remove the a candlestick callback
	 * @param symbol
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeCandlestickCallback(final ChannelSymbol symbol,
			final BiConsumer<ChannelSymbol, Tick> callback) throws APIException {
		
		return candleCallbacks.removeCallback(symbol, callback);
	}
	

	/**
	 * Process a list with candlesticks
	 * @param symbol
	 * @param ticksBuffer
	 */
	public void handleCandlestickList(final ChannelSymbol symbol, final List<Tick> ticksBuffer) {
		candleCallbacks.handleEventsList(symbol, ticksBuffer);
	}
	
	/**
	 * Handle a new candlestick
	 * @param currencyPair
	 * @param tick
	 */
	public void handleNewCandlestick(final ChannelSymbol currencyPair, final Tick tick) {
		
		synchronized (lastTick) {
			lastTick.put(currencyPair, tick);
			lastTickTimestamp.put(currencyPair, System.currentTimeMillis());
		}
		
		candleCallbacks.handleEvent(currencyPair, tick);
	}

	/**
	 * Subscribe candles for a symbol
	 * @param command
	 */
	public void subscribeCandles(AbstractAPICommand command) {
		apiBroker.sendCommand(command);
	}

	/**
	 * Unsubscribe the candles
	 * @param command
	 * @param symbol
	 */
	public void unsubscribeCandles(final AbstractAPICommand command, final ChannelSymbol symbol) {

		final int channel = apiBroker.getChannelForSymbol(symbol);

		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + symbol);
		}

		apiBroker.sendCommand(command);
		apiBroker.removeChannelForSymbol(symbol);
	}
	
}

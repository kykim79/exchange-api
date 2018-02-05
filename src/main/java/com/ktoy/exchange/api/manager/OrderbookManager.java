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

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.OrderbookConfiguration;
import com.ktoy.exchange.api.entity.OrderbookEntry;

public class OrderbookManager {

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<OrderbookConfiguration, OrderbookEntry> channelCallbacks;

	/**
	 * The executor service
	 */
	private final ExecutorService executorService;

	/**
	 * The bitfinex API broker
	 */
	private final ApiBroker apiBroker;

	public OrderbookManager(final ApiBroker apiBroker) {
		this.apiBroker = apiBroker;
		this.executorService = apiBroker.getExecutorService();
		this.channelCallbacks = new BiConsumerCallbackManager<>(executorService);
	}
	
	/**
	 * Register a new trading orderbook callback
	 * @param orderbookConfiguration
	 * @param callback
	 * @throws APIException
	 */
	public void registerOrderbookCallback(final OrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback) throws APIException {
		
		channelCallbacks.registerCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Remove the a trading orderbook callback
	 * @param orderbookConfiguration
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeOrderbookCallback(final OrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback) throws APIException {
		
		return channelCallbacks.removeCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Subscribe a orderbook
	 * @param apiCommand
	 *
	 */
	public void subscribeOrderbook(AbstractAPICommand apiCommand) {
		apiBroker.sendCommand(apiCommand);
	}
	
	/**
	 * Unsubscribe a orderbook
	 * @param apiCommand
	 * @param orderbookConfiguration
	 */
	public void unsubscribeOrderbook(AbstractAPICommand apiCommand, final OrderbookConfiguration orderbookConfiguration) {
		
		final int channel = apiBroker.getChannelForSymbol(orderbookConfiguration);
		
		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + orderbookConfiguration);
		}

		apiBroker.sendCommand(apiCommand);
		apiBroker.removeChannelForSymbol(orderbookConfiguration);
	}
	
	/**
	 * Handle a new orderbook entry
	 * @param configuration
	 * @param entry
	 */
	public void handleNewOrderbookEntry(final OrderbookConfiguration configuration, 
			final OrderbookEntry entry) {
		
		channelCallbacks.handleEvent(configuration, entry);
	}
}

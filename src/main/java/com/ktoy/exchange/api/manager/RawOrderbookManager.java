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
import com.ktoy.exchange.api.commands.BitfinexSubscribeRawOrderbookCommand;
import com.ktoy.exchange.api.commands.BitfinexUnsubscribeChannelCommand;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.RawOrderbookConfiguration;
import com.ktoy.exchange.api.entity.RawOrderbookEntry;

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

public class RawOrderbookManager {

	/**
	 * The channel callbacks
	 */
	private final BiConsumerCallbackManager<RawOrderbookConfiguration, RawOrderbookEntry> channelCallbacks;

	/**
	 * The executor service
	 */
	private final ExecutorService executorService;

	/**
	 * The bitfinex API broker
	 */
	private final ApiBroker apiBroker;

	public RawOrderbookManager(final ApiBroker apiBroker) {
		this.apiBroker = apiBroker;
		this.executorService = apiBroker.getExecutorService();
		this.channelCallbacks = new BiConsumerCallbackManager<>(executorService);
	}
	
	/**
	 * Register a new trading orderbook callback
	 * @param symbol
	 * @param callback
	 * @throws APIException
	 */
	public void registerOrderbookCallback(final RawOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<RawOrderbookConfiguration, RawOrderbookEntry> callback) throws APIException {
		
		channelCallbacks.registerCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Remove the a trading orderbook callback
	 * @param orderbookConfiguration
	 * @param callback
	 * @return
	 * @throws APIException
	 */
	public boolean removeOrderbookCallback(final RawOrderbookConfiguration orderbookConfiguration, 
			final BiConsumer<RawOrderbookConfiguration, RawOrderbookEntry> callback) throws APIException {
		
		return channelCallbacks.removeCallback(orderbookConfiguration, callback);
	}
	
	/**
	 * Subscribe a orderbook
	 * @param orderbookConfiguration
	 */
	public void subscribeOrderbook(final RawOrderbookConfiguration orderbookConfiguration) {
		
		final BitfinexSubscribeRawOrderbookCommand subscribeOrderbookCommand
			= new BitfinexSubscribeRawOrderbookCommand(orderbookConfiguration);
		
		apiBroker.sendCommand(subscribeOrderbookCommand);
	}
	
	/**
	 * Unsubscribe a orderbook
	 * @param orderbookConfiguration
	 */
	public void unsubscribeOrderbook(final RawOrderbookConfiguration orderbookConfiguration) {
		
		final int channel = apiBroker.getChannelForSymbol(orderbookConfiguration);
		
		if(channel == -1) {
			throw new IllegalArgumentException("Unknown symbol: " + orderbookConfiguration);
		}
		
		final BitfinexUnsubscribeChannelCommand command = new BitfinexUnsubscribeChannelCommand(channel);
		apiBroker.sendCommand(command);
		apiBroker.removeChannelForSymbol(orderbookConfiguration);
	}
	
	/**
	 * Handle a new orderbook entry
	 * @param configuration
	 * @param entry
	 */
	public void handleNewOrderbookEntry(final RawOrderbookConfiguration configuration, 
			final RawOrderbookEntry entry) {
		
		channelCallbacks.handleEvent(configuration, entry);
	}
}

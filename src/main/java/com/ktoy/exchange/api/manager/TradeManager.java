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
import com.ktoy.exchange.api.entity.Trade;

public class TradeManager extends SimpleCallbackManager<Trade>{

	/**
	 * The bitfinex API broker
	 */
	private final ApiBroker apiBroker;

	public TradeManager(final ApiBroker apiBroker) {
		super(apiBroker.getExecutorService());
		this.apiBroker = apiBroker;
	}
	
	/**
	 * Update a exchange order
	 * @param trade
	 */
	public void updateTrade(final Trade trade) {
		//trade.setApikey(apiBroker.getApiKey());
		notifyCallbacks(trade);
	}
	
}
	

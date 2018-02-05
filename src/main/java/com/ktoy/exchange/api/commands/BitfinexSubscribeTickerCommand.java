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
package com.ktoy.exchange.api.commands;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import org.json.JSONObject;

public class BitfinexSubscribeTickerCommand extends AbstractAPICommand {

	private String currencyPair;

	public BitfinexSubscribeTickerCommand(final ChannelSymbol currencyPair) {
		this.currencyPair = currencyPair.toSymbolString();
	}

	@Override
	public String getCommand(final ApiBroker apiBroker) {
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("event", "subscribe");
		subscribeJson.put("channel", "ticker");
		subscribeJson.put("symbol", currencyPair);
		
		return subscribeJson.toString();
	}
}

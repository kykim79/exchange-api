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
package com.ktoy.exchange.api.callback.command;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.entity.symbol.GdaxCurrencyPair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GdaxSubscribedCallback implements CommandCallbackHandler {

	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(GdaxSubscribedCallback.class);
	
	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONObject jsonObject) {

		//final String channel = jsonObject.getString("channel");
		final String type = jsonObject.getString("type");

		GdaxCurrencyPair pair = null;
		switch(type) {

			case "ticker":
			case "snapshot":
			case "heartbeat":
			case "l2update":
			case "received":
			case "open":
			case "done":
			case "match":
			case "change":
			case "activate":
				//currencyPair = GdaxCurrencyPair.fromSymbolString(jsonObject.getString("product_id"));
				//apiBroker.addToChannelSymbolMap(channelId, currencyPair);
				pair = GdaxCurrencyPair.fromSymbolString(jsonObject.getString("product_id"));
				apiBroker.addToChannelSymbolMap(pair.ordinal(), pair);
				break;
			case "subscriptions":
				logger.info("Subscribed {}", jsonObject.toString());
				break;
			default:
				logger.error("Unknown subscribed callback {}", jsonObject.toString());
		}

	}
}

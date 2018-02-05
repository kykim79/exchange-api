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
import com.ktoy.exchange.api.entity.OrderbookConfiguration;
import com.ktoy.exchange.api.entity.RawOrderbookConfiguration;
import com.ktoy.exchange.api.entity.symbol.BitfinexCandlestickSymbol;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribedCallback implements CommandCallbackHandler {

	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(SubscribedCallback.class);
	
	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONObject jsonObject) {
		
		final String channel = jsonObject.getString("channel");
		final int channelId = jsonObject.getInt("chanId");

		switch(channel) {
		case "ticker":
			final String symbol = jsonObject.getString("symbol");
			final BitfinexCurrencyPair currencyPair = BitfinexCurrencyPair.fromSymbolString(symbol);
			logger.info("Registering symbol {} on channel {}", currencyPair, channelId);
			apiBroker.addToChannelSymbolMap(channelId, currencyPair);
			break;
		case "candles":
			final String key = jsonObject.getString("key");
			logger.info("Registering key {} on channel {}", key, channelId);
			final BitfinexCandlestickSymbol candleStickSymbol = BitfinexCandlestickSymbol.fromString(key);
			apiBroker.addToChannelSymbolMap(channelId, candleStickSymbol);
			break;
		case "book":
			if("R0".equals(jsonObject.getString("prec"))) {
				final RawOrderbookConfiguration configuration 
					= RawOrderbookConfiguration.fromJSON(jsonObject);
				logger.info("Registering raw book {} on channel {}", jsonObject, channelId);
				apiBroker.addToChannelSymbolMap(channelId, configuration);
			} else {
				final OrderbookConfiguration configuration
					= OrderbookConfiguration.fromJSON(jsonObject);
				logger.info("Registering book {} on channel {}", jsonObject, channelId);
				apiBroker.addToChannelSymbolMap(channelId, configuration);
			}
			break;
		default:
			logger.error("Unknown subscribed callback {}", jsonObject.toString());
		}

	}
}

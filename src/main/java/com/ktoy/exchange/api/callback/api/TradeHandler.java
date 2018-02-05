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
package com.ktoy.exchange.api.callback.api;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.BitfinexOrderType;
import com.ktoy.exchange.api.entity.BitfinexOrderTypeV2;
import com.ktoy.exchange.api.entity.Trade;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.Channel;

public class TradeHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(TradeHandler.class);

	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONArray jsonArray) {

		logger.debug("Got trade callback {}", jsonArray.toString());

		// Executed or update
		boolean executed = true;

		ChannelSymbol channelSymbol = apiBroker.getFromChannelSymbolMap(jsonArray.getInt(0));
		final BitfinexCurrencyPair currencyPair =  BitfinexCurrencyPair.fromSymbolString(channelSymbol.toSymbolString());
		handleTradeCallback(apiBroker, jsonArray, executed, currencyPair);


	}


	/**
	 * Handle a single trade callback
	 * @param apiBroker
	 * @param jsonTrade
	 * @param currencyPair
	 * @throws APIException
	 */
	private void handleTradeCallback(final ApiBroker apiBroker, final JSONArray jsonTrade,
									 final boolean executed, BitfinexCurrencyPair currencyPair) {



		final Trade trade = new Trade();
//		trade.setExecuted(executed);
//		trade.setApikey(apiBroker.getApiKey());

		JSONArray subArray = jsonTrade.getJSONArray(2);

		trade.setId(subArray.getLong(0));
		trade.setCurrency(currencyPair);
		trade.setMtsCreate(subArray.getLong(1));
		trade.setOrderId(subArray.getLong(0));
		trade.setExecAmount(subArray.getFloat(2));
		trade.setExecPrice(subArray.getFloat(3));


		final String orderType = jsonTrade.getString(1);
		if(orderType != null) {
			trade.setOrderType(BitfinexOrderTypeV2.fromString(orderType));
		}

		trade.setOrderPrice(subArray.getFloat(3));
//		trade.setMaker(jsonTrade.getInt(8) == 1 ? true : false);
//		trade.setFee(jsonTrade.optFloat(9, -1));
//		trade.setFeeCurrency(jsonTrade.optString(10, ""));

		apiBroker.getQuoteManager().handleTrade(currencyPair, trade);
	}
}

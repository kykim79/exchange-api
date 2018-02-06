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
import com.ktoy.exchange.api.entity.BitfinexOrderTypeV2;
import com.ktoy.exchange.api.entity.BitfinexTrade;
import com.ktoy.exchange.api.entity.GdaxTrade;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import com.ktoy.exchange.api.entity.symbol.GdaxCurrencyPair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

public class GdaxTradeHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(GdaxTradeHandler.class);

	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONArray jsonArray)
			throws APIException {

		logger.debug("Got trade callback {}", jsonArray.toString());

		// Executed or update
		boolean executed = true;

		JSONObject jsonObject = jsonArray.getJSONObject(0);
		ChannelSymbol channelSymbol = GdaxCurrencyPair.fromSymbolString(jsonObject.getString("product_id"));
		final GdaxCurrencyPair currencyPair =  GdaxCurrencyPair.fromSymbolString(channelSymbol.toSymbolString());
		handleTradeCallback(apiBroker, jsonObject, executed, currencyPair);
	}

	/**
	 * Handle a single trade callback
	 * @param apiBroker
	 * @param jsonObject
	 * @param currencyPair
	 * @throws APIException
	 */
	private void handleTradeCallback(final ApiBroker apiBroker, final JSONObject jsonObject,
									 final boolean executed, GdaxCurrencyPair currencyPair) {



		final GdaxTrade trade = new GdaxTrade();

		trade.setId(jsonObject.getLong("sequence"));
		trade.setCurrency(currencyPair);

		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");
			trade.setMtsCreate(df.parse(jsonObject.getString("time")).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		trade.setOrderId(jsonObject.getLong("trade_id"));
		trade.setExecAmount(jsonObject.getFloat("size"));
		trade.setExecPrice(jsonObject.getFloat("price"));
		trade.setOrderType(jsonObject.getString("side").equalsIgnoreCase("sell") ? "buy" : "sell");
		trade.setOrderPrice(jsonObject.getFloat("price"));
		apiBroker.getQuoteManager().handleTrade(currencyPair, trade);
	}
}

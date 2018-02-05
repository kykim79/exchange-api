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
import com.ktoy.exchange.api.entity.Trade;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(TradeHandler.class);

	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONArray jsonArray)
			throws APIException {

		logger.info("Got trade callback {}", jsonArray.toString());
		
		final JSONArray trade = jsonArray.getJSONArray(2);
		final String type = jsonArray.getString(1);
		
		// Executed or update
		boolean executed = true;
		if("tu".equals(type)) {
			executed = false;
		}

		handleTradeCallback(apiBroker, trade, executed);
	}


	/**
	 * Handle a single trade callback
	 * @param apiBroker
	 * @param jsonTrade
	 * @throws APIException 
	 */
	private void handleTradeCallback(final ApiBroker apiBroker, final JSONArray jsonTrade,
			final boolean executed) throws APIException {		
		
		final Trade trade = new Trade();
		trade.setExecuted(executed);
		trade.setApikey(apiBroker.getApiKey());
		trade.setId(jsonTrade.getLong(0));
		trade.setCurrency(BitfinexCurrencyPair.fromSymbolString(jsonTrade.getString(1)));
		trade.setMtsCreate(jsonTrade.getLong(2));
		trade.setOrderId(jsonTrade.getLong(3));
		trade.setExecAmount(jsonTrade.getFloat(4));
		trade.setExecPrice(jsonTrade.getFloat(5));
		
		final String orderTypeString = jsonTrade.optString(6, null);
		
		if(orderTypeString != null) {
			trade.setOrderType(BitfinexOrderType.fromString(orderTypeString));
		}
		
		trade.setOrderPrice(jsonTrade.optFloat(7, -1));
		trade.setMaker(jsonTrade.getInt(8) == 1 ? true : false);
		trade.setFee(jsonTrade.optFloat(9, -1));
		trade.setFeeCurrency(jsonTrade.optString(10, ""));

		apiBroker.getTradeManager().updateTrade(trade);
	}
}

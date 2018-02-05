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
import com.ktoy.exchange.api.entity.BitfinexExchangeOrder;
import com.ktoy.exchange.api.entity.ExchangeOrderState;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationHandler implements APICallbackHandler {

	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(NotificationHandler.class);
	
	@Override
	public void 	handleChannelData(final ApiBroker apiBroker, final JSONArray jsonArray) {
		
		logger.debug("Got notification callback {}", jsonArray.toString());

		final JSONArray notifificationValue = jsonArray.optJSONArray(2);

		// Test for order error callback
		// [0,"n",[null,"on-req",null,null,[null,null,1513970684865000,"tBTCUSD",null,null,0.001,0.001,"EXCHANGE MARKET",null,null,null,null,null,null,null,12940,null,null,null,null,null,null,0,null,null],null,"ERROR","Invalid order: minimum size for BTC/USD is 0.002"]]
		if(notifificationValue != null) {
			if("on-req".equals(notifificationValue.getString(1))) {
				
				final JSONArray order = notifificationValue.optJSONArray(4);
				final String state = notifificationValue.optString(6);
				final String stateValue = notifificationValue.optString(7);

				if("ERROR".equals(state)) {
					final BitfinexExchangeOrder exchangeOrder = new BitfinexExchangeOrder();
					exchangeOrder.setApikey(apiBroker.getApiKey());
					exchangeOrder.setCid(order.getLong(2));
					exchangeOrder.setSymbol(order.getString(3));
					exchangeOrder.setState(ExchangeOrderState.STATE_ERROR);
					
					logger.error("State for order {} is {}, reason is {}", exchangeOrder.getOrderId(), state, stateValue);

					apiBroker.getOrderManager().updateOrder(exchangeOrder);
				}
			}
		}
	}

}

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
import com.ktoy.exchange.api.entity.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class GdaxOrderHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(GdaxOrderHandler.class);

	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONArray jsonArray)
			throws APIException {

		logger.info("Got order callback {}", jsonArray.toString());
		

		
		// No orders active
		if(jsonArray.length() == 0) {
			notifyOrderLatch(apiBroker);
			return;
		}

		JSONObject obj = jsonArray.getJSONObject(0);
		JSONArray orders = obj.getJSONArray("changes");

		// Snapshot or update
		if(! (orders instanceof JSONArray)) {
			handleOrderCallback(apiBroker, obj, orders);
		} else {
			orders.forEach(e -> {
				handleOrderCallback(apiBroker, obj, (JSONArray)e);
			});

			notifyOrderLatch(apiBroker);
		}
	}

	/**
	 * Notify the order latch
	 * @param apiBroker
	 */
	private void notifyOrderLatch(final ApiBroker apiBroker) {
		
		// All snapshots are completed
		final CountDownLatch connectionReadyLatch = apiBroker.getConnectionReadyLatch();
		
		if(connectionReadyLatch != null) {
			connectionReadyLatch.countDown();
		}
	}

	/**
	 * Handle a single order callback
	 * @param apiBroker
	 * @param object
	 * @throws APIException 
	 */
	private void handleOrderCallback(ApiBroker apiBroker, final JSONObject object, final JSONArray order) {
		final ExchangeOrder exchangeOrder = new GdaxExchangeOrder();
		exchangeOrder.setSymbol(object.getString("product_id"));
		exchangeOrder.setOrderType(GdaxOrderType.fromString(order.getString(0)));
		exchangeOrder.setPrice(order.getDouble(1));
		exchangeOrder.setAmount(order.getDouble(2));

		apiBroker.getOrderManager().updateOrder(exchangeOrder);
	}
}

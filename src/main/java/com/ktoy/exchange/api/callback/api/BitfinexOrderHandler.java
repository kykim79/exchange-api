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
import com.ktoy.exchange.api.entity.BitfinexExchangeOrder;
import com.ktoy.exchange.api.entity.ExchangeOrderState;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class BitfinexOrderHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(BitfinexOrderHandler.class);

	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONArray jsonArray)
			throws APIException {

		logger.info("Got order callback {}", jsonArray.toString());
		
		final JSONArray orders = jsonArray.getJSONArray(2);
		
		// No orders active
		if(orders.length() == 0) {
			notifyOrderLatch(apiBroker);
			return;
		}
		
		// Snapshot or update
		if(! (orders.get(0) instanceof JSONArray)) {
			handleOrderCallback(apiBroker, orders);
		} else {
			for(int orderPos = 0; orderPos < orders.length(); orderPos++) {
				final JSONArray orderArray = orders.getJSONArray(orderPos);
				handleOrderCallback(apiBroker, orderArray);
			}
			
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
	 * @param order
	 * @throws APIException 
	 */
	private void handleOrderCallback(ApiBroker apiBroker, final JSONArray order) throws APIException {
		final BitfinexExchangeOrder exchangeOrder = new BitfinexExchangeOrder();
		exchangeOrder.setApikey(apiBroker.getApiKey());
		exchangeOrder.setOrderId(order.getLong(0));
		exchangeOrder.setGroupId(order.optInt(1, -1));
		exchangeOrder.setCid(order.optLong(2, -1));
		exchangeOrder.setSymbol(order.getString(3));
		exchangeOrder.setCreated(order.getLong(4));
		exchangeOrder.setUpdated(order.getLong(5));
		exchangeOrder.setAmount(order.getDouble(6));
		exchangeOrder.setAmountAtCreation(order.getDouble(7));
		exchangeOrder.setOrderType(BitfinexOrderType.fromString(order.getString(8)));
		
		final ExchangeOrderState orderState = ExchangeOrderState.fromString(order.getString(13));
		exchangeOrder.setState(orderState);
		
		exchangeOrder.setPrice(order.getDouble(16));
		exchangeOrder.setPriceAvg(order.optDouble(17, -1));
		exchangeOrder.setPriceTrailing(order.optDouble(18, -1));
		exchangeOrder.setPriceAuxLimit(order.optDouble(19, -1));
		exchangeOrder.setNotify(order.getInt(23) == 1 ? true : false);
		exchangeOrder.setHidden(order.getInt(24) == 1 ? true : false);

		apiBroker.getOrderManager().updateOrder(exchangeOrder);
	}
}

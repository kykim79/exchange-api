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
import com.ktoy.exchange.api.entity.Position;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class PositionHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(PositionHandler.class);

	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONArray jsonArray)
			throws APIException {
		
		logger.info("Got position callback {}", jsonArray.toString());
		
		final JSONArray positions = jsonArray.getJSONArray(2);
		
		// No positons active
		if(positions.length() == 0) {
			notifyLatch(apiBroker);
			return;
		}
		
		// Snapshot or update
		if(! (positions.get(0) instanceof JSONArray)) {
			handlePositionCallback(apiBroker, positions);
		} else {
			for(int orderPos = 0; orderPos < positions.length(); orderPos++) {
				final JSONArray orderArray = positions.getJSONArray(orderPos);
				handlePositionCallback(apiBroker, orderArray);
			}
		}		
		
		notifyLatch(apiBroker);
	}

	/**
	 * Notify the snapshot latch
	 * @param apiBroker
	 */
	private void notifyLatch(final ApiBroker apiBroker) {
		
		// All snapshots are completed
		final CountDownLatch connectionReadyLatch = apiBroker.getConnectionReadyLatch();
		
		if(connectionReadyLatch != null) {
			connectionReadyLatch.countDown();
		}
	}

	/**
	 * Handle a position update
	 * @param apiBroker
	 * @param positions
	 */
	private void handlePositionCallback(final ApiBroker apiBroker, final JSONArray positions) {
		final String currencyString = positions.getString(0);
		BitfinexCurrencyPair currency = BitfinexCurrencyPair.fromSymbolString(currencyString);
				
		final Position position = new Position(currency);
		position.setStatus(positions.getString(1));
		position.setAmount(positions.getDouble(2));
		position.setBasePrice(positions.getDouble(3));
		position.setMarginFunding(positions.getDouble(4));
		position.setMarginFundingType(positions.getDouble(5));
		position.setPl(positions.optDouble(6, -1));
		position.setPlPercent(positions.optDouble(7, -1));
		position.setPriceLiquidation(positions.optDouble(8, -1));
		position.setLeverage(positions.optDouble(9, -1));
				
		apiBroker.getPositionManager().updatePosition(position);
	}

}

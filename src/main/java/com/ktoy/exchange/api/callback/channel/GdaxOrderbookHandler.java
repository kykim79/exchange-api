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
package com.ktoy.exchange.api.callback.channel;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.OrderbookConfiguration;
import com.ktoy.exchange.api.entity.OrderbookEntry;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import org.json.JSONArray;
import org.json.JSONException;

public class GdaxOrderbookHandler implements ChannelCallbackHandler {

	@Override
	public void handleChannelData(final ApiBroker apiBroker,
								  final ChannelSymbol channelSymbol, final JSONArray jsonArray) throws APIException {
		
		final OrderbookConfiguration configuration = (OrderbookConfiguration) channelSymbol;
		
		// Example: [13182,1,-0.1]
		try {
			// Snapshots contain multiple Orderbook entries, updates only one
			if(jsonArray.get(0) instanceof JSONArray) {
				for (int pos = 0; pos < jsonArray.length(); pos++) {
					final JSONArray parts = jsonArray.getJSONArray(pos);	
					handleEntry(apiBroker, configuration, parts);
				}
			} else {
				handleEntry(apiBroker, configuration, jsonArray);
			}
			
		} catch (JSONException e) {
			throw new APIException(e);
		} 
	}

	/**
	 * Handle a new orderbook entry
	 * @param apiBroker
	 * @param configuration
	 * @param jsonArray
	 */
	private void handleEntry(final ApiBroker apiBroker,
			final OrderbookConfiguration configuration,
			final JSONArray jsonArray) {
		
		final double price = jsonArray.getNumber(0).doubleValue();
		final double count = jsonArray.getNumber(1).doubleValue();
		final double amount = jsonArray.getNumber(2).doubleValue();
		
		final OrderbookEntry orderbookEntry = new OrderbookEntry(price, count, amount);
		
		apiBroker.getOrderbookManager().handleNewOrderbookEntry(configuration, orderbookEntry);
	}

}

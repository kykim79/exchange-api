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
package com.ktoy.exchange.api.commands;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import org.json.JSONArray;
import org.json.JSONObject;

public class GdaxSubscribeTickerCommand extends AbstractAPICommand {

	private final ChannelSymbol symbol;

	public GdaxSubscribeTickerCommand(final ChannelSymbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getCommand(final ApiBroker apiBroker) {
		final JSONObject subscribe = new JSONObject();
		subscribe.put("type", "subscribe");
		JSONObject ticker = new JSONObject();
		ticker.put("name", "ticker");
		JSONArray product_ids = new JSONArray();
		product_ids.put(symbol.toSymbolString());
		ticker.put("product_ids", product_ids);
		JSONArray channels = new JSONArray();
		channels.put("heartbeat");
		channels.put(ticker);
		subscribe.put("channels", channels);
		JSONArray channels2 = new JSONArray();
		channels2.put(symbol.toSymbolString());

		subscribe.put("product_ids", channels2);
				
		return subscribe.toString();
	}

}

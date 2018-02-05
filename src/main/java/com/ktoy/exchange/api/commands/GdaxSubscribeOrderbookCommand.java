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

public class GdaxSubscribeOrderbookCommand extends AbstractAPICommand {

	private final ChannelSymbol symbol;

	public GdaxSubscribeOrderbookCommand(final ChannelSymbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getCommand(final ApiBroker apiBroker) {
//		final JSONObject json = new JSONObject();
//		json.put("type", "snapshot");
//		JSONArray array = new JSONArray();
//		array.put(symbol.toSymbolString());
//		json.put("product_ids", array);

		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("type", "subscribe");

//		JSONObject obj = new JSONObject();
//		obj.put("name", "ticker");

		JSONArray product_ids = new JSONArray();
		product_ids.put(symbol.toSymbolString());
		subscribeJson.put("product_ids", product_ids);

		JSONArray channels = new JSONArray();
		channels.put("level2");
		subscribeJson.put("channels", channels);

				
		return subscribeJson.toString();
	}

}

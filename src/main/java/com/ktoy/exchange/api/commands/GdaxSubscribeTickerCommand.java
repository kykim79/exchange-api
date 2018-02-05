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
		final JSONObject subscribeJson = new JSONObject();
		subscribeJson.put("type", "subscribe");
		JSONObject obj = new JSONObject();
		obj.put("name", "ticker");
		JSONArray array = new JSONArray();
		array.put(symbol.toSymbolString());
		obj.put("product_ids", array);
		JSONArray array1 = new JSONArray();
		array1.put(obj);
		subscribeJson.put("channels", array1);
		JSONArray array2 = new JSONArray();
		array2.put(symbol.toSymbolString());
		subscribeJson.put("product_ids", array2);
				
		return subscribeJson.toString();
	}

}

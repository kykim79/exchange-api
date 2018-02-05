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
import com.ktoy.exchange.api.entity.BitfinexOrder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitfinexOrderCommand extends AbstractAPICommand {

	private final BitfinexOrder bitfinexOrder;
	
	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(BitfinexOrderCommand.class);

	public BitfinexOrderCommand(final BitfinexOrder bitfinexOrder) {
		this.bitfinexOrder = bitfinexOrder;
	}

	@Override
	public String getCommand(final ApiBroker apiBroker) throws CommandException {
		
		final JSONObject orderJson = new JSONObject();
		orderJson.put("cid", bitfinexOrder.getCid());
		orderJson.put("type", bitfinexOrder.getType());
		orderJson.put("symbol", bitfinexOrder.getSymbol().toSymbolString());
		orderJson.put("amount", Double.toString(bitfinexOrder.getAmount()));
		
		if(bitfinexOrder.getPrice() != -1) {
			orderJson.put("price", Double.toString(bitfinexOrder.getPrice()));
		}

		if(bitfinexOrder.getPriceTrailing() != -1) {
			orderJson.put("price_trailing", bitfinexOrder.getPriceTrailing());
		}
		
		if(bitfinexOrder.getPriceAuxLimit() != -1) {
			orderJson.put("price_aux_limit", bitfinexOrder.getPriceAuxLimit());
		}
		
		if(bitfinexOrder.isHidden()) {
			orderJson.put("hidden", 1);
		} else {
			orderJson.put("hidden", 0);
		}
		
		if(bitfinexOrder.isPostOnly()) {
			orderJson.put("postonly", 1);
		}
		
		if(bitfinexOrder.getGroupId() > 0) {
			orderJson.put("gid", bitfinexOrder.getGroupId());
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("[0,\"on\", null, ");
		sb.append(orderJson.toString());
		sb.append("]\n");
		
		logger.debug(sb.toString());
		
		return sb.toString();
	}

}

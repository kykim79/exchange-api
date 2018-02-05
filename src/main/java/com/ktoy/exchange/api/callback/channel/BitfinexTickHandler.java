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
import com.ktoy.exchange.api.Const;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import org.json.JSONArray;
import org.ta4j.core.BaseTick;
import org.ta4j.core.Tick;

import java.time.ZonedDateTime;

public class BitfinexTickHandler implements ChannelCallbackHandler {

	/**
	 * Handle a tick callback
	 * @param channelSymbol
	 * @param jsonArray
	 */
	@Override
	public void handleChannelData(final ApiBroker apiBroker,
								  final ChannelSymbol channelSymbol, final JSONArray jsonArray) throws APIException {

		final BitfinexCurrencyPair currencyPair = (BitfinexCurrencyPair) channelSymbol;
		
		// 0 = BID
		// 2 = ASK
		// 6 = Price
		final double price = jsonArray.getDouble(6);
		
		// Volume is set to 0, because the ticker contains only the daily volume
		final Tick tick = new BaseTick(ZonedDateTime.now(Const.BITFINEX_TIMEZONE), price, price, 
				price, price, 0);
		
		apiBroker.getQuoteManager().handleNewTick(currencyPair, tick);
	}
}

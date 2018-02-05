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
package com.ktoy.exchange.api.entity;

import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import org.json.JSONObject;

public class RawOrderbookConfiguration implements ChannelSymbol {
	
	/**
	 * The currency pair
	 */
	protected final BitfinexCurrencyPair currencyPair;
	
	public RawOrderbookConfiguration(final BitfinexCurrencyPair currencyPair) {
		this.currencyPair = currencyPair;
	}

	@Override
	public String toSymbolString() {
		return "RawOrderbookConfiguration [currencyPair=" + currencyPair + "]";
	}

	public BitfinexCurrencyPair getCurrencyPair() {
		return currencyPair;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currencyPair == null) ? 0 : currencyPair.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RawOrderbookConfiguration other = (RawOrderbookConfiguration) obj;
		if (currencyPair != other.currencyPair)
			return false;
		return true;
	}

	/**
	 * Build from JSON Array
	 * @param jsonObject
	 * @return
	 */
	public static RawOrderbookConfiguration fromJSON(final JSONObject jsonObject) {
		return new RawOrderbookConfiguration(
				BitfinexCurrencyPair.fromSymbolString(jsonObject.getString("symbol")));
	}
	
}

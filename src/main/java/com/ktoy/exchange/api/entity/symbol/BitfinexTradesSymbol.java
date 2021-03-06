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
package com.ktoy.exchange.api.entity.symbol;

import com.ktoy.exchange.api.entity.Timeframe;

public class BitfinexTradesSymbol implements ChannelSymbol {

	/**
	 * The symbol
	 */
	private final BitfinexCurrencyPair symbol;


	public BitfinexTradesSymbol(final BitfinexCurrencyPair symbol) {
		this.symbol = symbol;
	}

	public BitfinexCurrencyPair getSymbol() {
		return symbol;
	}

	/**
	 * To Bitfinex symbol string
	 * @return
	 */
	public String toSymbolString() { return symbol.toSymbolString();
	}
	
	/**
	 * Construct from Bitfinex string
	 * @param symbol
	 * @return
	 */
	public static BitfinexTradesSymbol fromString(final String symbol) {

		if(! symbol.startsWith("t")) {
			throw new IllegalArgumentException("Unable to parse: " + symbol);
		}
		
		return new BitfinexTradesSymbol(
				BitfinexCurrencyPair.fromSymbolString(symbol));
	}

	@Override
	public String toString() {
		return "BitfinexTradesSymbol [symbol=" + symbol + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
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
		final BitfinexTradesSymbol other = (BitfinexTradesSymbol) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;

		return true;
	}


}

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

public class GdaxCandlestickSymbol implements ChannelSymbol {

	/**
	 * The symbol
	 */
	private final GdaxCurrencyPair symbol;

	/**
	 * The timeframe
	 */
	private final Timeframe timeframe;

	public GdaxCandlestickSymbol(final GdaxCurrencyPair symbol, final Timeframe timeframe) {
		this.symbol = symbol;
		this.timeframe = timeframe;
	}

	public GdaxCurrencyPair getSymbol() {
		return symbol;
	}

	public Timeframe getTimeframe() {
		return timeframe;
	}
	
	/**
	 * To Bitfinex symbol string
	 * @return
	 */
	public String toSymbolString() {
		return symbol.toSymbolString();
	}
	
	/**
	 * Construct from Bitfinex string
	 * @param symbol
	 * @return
	 */
	public static GdaxCandlestickSymbol fromString(final String symbol) {
		
		if(! symbol.startsWith("trade:")) {
			throw new IllegalArgumentException("Unable to parse: " + symbol);
		}
		
		final String[] splitString = symbol.split(":");

		if(splitString.length != 3) {
			throw new IllegalArgumentException("Unable to parse: " + symbol);
		}
		
		final String timeframeString = splitString[1];
		final String symbolString = splitString[2];
		
		return new GdaxCandlestickSymbol(
				GdaxCurrencyPair.fromSymbolString(symbolString),
				Timeframe.fromSymbolString(timeframeString));
	}

	@Override
	public String toString() {
		return "GdaxCandlestickSymbol [symbol=" + symbol + ", timeframe=" + timeframe + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((timeframe == null) ? 0 : timeframe.hashCode());
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
		final GdaxCandlestickSymbol other = (GdaxCandlestickSymbol) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (timeframe != other.timeframe)
			return false;
		return true;
	}
	
	
}

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

public enum BitfinexOrderType implements OrderType {

	MARKET("MARKET"), 
	EXCHANGE_MARKET("EXCHANGE MARKET"), 
	LIMIT("LIMIT"), 
	EXCHANGE_LIMIT("EXCHANGE LIMIT"), 
	STOP("STOP"), 
	EXCHANGE_STOP("EXCHANGE STOP"), 
	TRAILING_STOP("TRAILING STOP"), 
	EXCHANGE_TRAILING_STOP("EXCHANGE TRAILING STOP"), 
	FOK("FOK"), 
	EXCHANGE_FOK("EXCHANGE FOK"), 
	STOP_LIMIT("STOP LIMIT"), 
	EXCHANGE_STOP_LIMIT("EXCHANGE STOP LIMIT");

	private final String bifinexString;

	private BitfinexOrderType(final String bifinexString) {
		this.bifinexString = bifinexString;
	}

	public String getBifinexString() {
		return bifinexString;
	}

	public static BitfinexOrderType fromString(String orderTypeText) {
		for (BitfinexOrderType orderType : BitfinexOrderType.values()) {
			if (orderType.getBifinexString().equalsIgnoreCase(orderTypeText)) {
				return orderType;
			}
		}
		throw new IllegalArgumentException("Unable to find order type for: " + orderTypeText);
	}
}

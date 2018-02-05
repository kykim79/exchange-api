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

public enum GdaxOrderType implements OrderType {

	BUY("BUY"),
	SELL("SELL");

	private final String orderType;

	private GdaxOrderType(final String orderType) {
		this.orderType = orderType;
	}

	public String getOrderType() {
		return orderType;
	}

	public static GdaxOrderType fromString(String orderTypeText) {
		for (GdaxOrderType orderType : GdaxOrderType.values()) {
			if (orderType.getOrderType().equalsIgnoreCase(orderTypeText)) {
				return orderType;
			}
		}
		throw new IllegalArgumentException("Unable to find order type for: " + orderTypeText);
	}
}

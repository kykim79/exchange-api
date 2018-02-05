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

public class RawOrderbookEntry {

	private final long orderId;
	private final double price;
	private final double amount;

	public RawOrderbookEntry(final long orderId, final double price, final double amount) {
		this.orderId = orderId;
		this.price = price;
		this.amount = amount;
	}

	public double getOrderId() {
		return orderId;
	}

	public double getPrice() {
		return price;
	}

	public double getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "RawOrderbookEntry [orderId=" + orderId + ", price=" + price + ", amount=" + amount + "]";
	}

}

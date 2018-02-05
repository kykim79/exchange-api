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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
public class GdaxExchangeOrder implements ExchangeOrder {

	@Id
	@GeneratedValue
	private long id;

	private String apikey;
	private long orderId;

	private long cid;


	private String symbol;
	private OrderType orderType;
	private double amount;
	private double price;

	/**
	 * Needed for hibernate
	 */
	public GdaxExchangeOrder() {

	}
	
	public long getOrderId() {
		return orderId;
	}

	@Override
	public ExchangeOrderState getState() {
		return null;
	}


	public long getCid() {
		return cid;
	}

	public void setCid(final long cid) {
		this.cid = cid;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(final String symbol) {
		this.symbol = symbol;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(final double amount) {
		this.amount = amount;
	}


	public double getPrice() {
		return price;
	}

	public void setPrice(final double price) {
		this.price = price;
	}


	public String getApikey() {
		return apikey;
	}

	public void setApikey(final String apikey) {
		this.apikey = apikey;
	}


	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
}

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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;

@Entity
@Table(name = "trades")
public class Trade {

	@Id
	@GeneratedValue
	private long id;

	private BitfinexCurrencyPair currency;
	private long mtsCreate;
	private long orderId;
	private float execAmount;
	private float execPrice;
	
	@Enumerated(EnumType.STRING)
	private BitfinexOrderTypeV2 orderType;
	
	private float orderPrice;

	/**
	 * Needed for hibernate
	 */
	public Trade() {

	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public BitfinexCurrencyPair getCurrency() {
		return currency;
	}

	public void setCurrency(final BitfinexCurrencyPair currency) {
		this.currency = currency;
	}

	public long getMtsCreate() {
		return mtsCreate;
	}

	public void setMtsCreate(final long mtsCreate) {
		this.mtsCreate = mtsCreate;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(final long orderId) {
		this.orderId = orderId;
	}

	public float getExecAmount() {
		return execAmount;
	}

	public void setExecAmount(final float execAmount) {
		this.execAmount = execAmount;
	}

	public float getExecPrice() {
		return execPrice;
	}

	public void setExecPrice(final float execPrice) {
		this.execPrice = execPrice;
	}

	public BitfinexOrderTypeV2 getOrderType() {
		return orderType;
	}

	public void setOrderType(final BitfinexOrderTypeV2 orderType) {
		this.orderType = orderType;
	}

	public float getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(final float orderPrice) {
		this.orderPrice = orderPrice;
	}

	@Override
	public String toString() {
		return "Trade [id=" + id + ", currency=" + currency
				+ ", mtsCreate=" + mtsCreate + ", orderId=" + orderId + ", execAmount=" + execAmount + ", execPrice="
				+ execPrice + ", orderType=" + orderType + ", orderPrice=" + orderPrice;
	}

}

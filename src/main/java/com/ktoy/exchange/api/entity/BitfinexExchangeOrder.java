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

@Entity
@Table(name = "orders")
public class BitfinexExchangeOrder implements ExchangeOrder {

	@Id
	@GeneratedValue
	private long id;
	
	private String apikey;
	private long orderId;
	private int groupId;
	private long cid;
	private String symbol;
	private long created;
	private long updated;
	private double amount;
	private double amountAtCreation;
	
	@Enumerated(EnumType.STRING)
	private OrderType orderType;
	
	@Enumerated(EnumType.STRING)
	private ExchangeOrderState state;
	
	private double price;
	private double priceAvg;
	private double priceTrailing;
	private double priceAuxLimit;
	private boolean notify;
	private boolean hidden;

	/**
	 * Needed for hibernate
	 */
	public BitfinexExchangeOrder() {

	}
	
	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(final long orderId) {
		this.orderId = orderId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(final int groupId) {
		this.groupId = groupId;
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

	public long getCreated() {
		return created;
	}

	public void setCreated(final long created) {
		this.created = created;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(final long updated) {
		this.updated = updated;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(final double amount) {
		this.amount = amount;
	}

	public double getAmountAtCreation() {
		return amountAtCreation;
	}

	public void setAmountAtCreation(final double amountAtCreation) {
		this.amountAtCreation = amountAtCreation;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(final OrderType orderType) {
		this.orderType = orderType;
	}

	public ExchangeOrderState getState() {
		return state;
	}

	public void setState(final ExchangeOrderState state) {
		this.state = state;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(final double price) {
		this.price = price;
	}

	public double getPriceAvg() {
		return priceAvg;
	}

	public void setPriceAvg(final double priceAvg) {
		this.priceAvg = priceAvg;
	}

	public double getPriceTrailing() {
		return priceTrailing;
	}

	public void setPriceTrailing(final double priceTrailing) {
		this.priceTrailing = priceTrailing;
	}

	public double getPriceAuxLimit() {
		return priceAuxLimit;
	}

	public void setPriceAuxLimit(final double priceAuxLimit) {
		this.priceAuxLimit = priceAuxLimit;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(final boolean notify) {
		this.notify = notify;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(final String apikey) {
		this.apikey = apikey;
	}

	@Override
	public String toString() {
		return "ExchangeOrder [id=" + id + ", apikey=" + apikey + ", orderId=" + orderId + ", groupId=" + groupId
				+ ", cid=" + cid + ", symbol=" + symbol + ", created=" + created + ", updated=" + updated + ", amount="
				+ amount + ", amountAtCreation=" + amountAtCreation + ", orderType=" + orderType + ", state=" + state
				+ ", price=" + price + ", priceAvg=" + priceAvg + ", priceTrailing=" + priceTrailing
				+ ", priceAuxLimit=" + priceAuxLimit + ", notify=" + notify + ", hidden=" + hidden + "]";
	}

}

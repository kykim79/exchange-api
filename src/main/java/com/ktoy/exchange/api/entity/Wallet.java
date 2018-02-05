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

public class Wallet {

	private final String walletType;
	private final String curreny;
	private final double balance;
	private final double unsettledInterest;
	private final double balanceAvailable;
	
	public final static String WALLET_TYPE_EXCHANGE = "exchange";
	
	public final static String WALLET_TYPE_MARGIN = "margin";
	
	public final static String WALLET_TYPE_FUNDING = "funding";

	public Wallet(final String walletType, final String curreny, final double balance, 
			final double unsettledInterest, final double balanceAvailable) {
		
		this.walletType = walletType;
		this.curreny = curreny;
		this.balance = balance;
		this.unsettledInterest = unsettledInterest;
		this.balanceAvailable = balanceAvailable;
	}

	@Override
	public String toString() {
		return "Wallet [walletType=" + walletType + ", curreny=" + curreny + ", balance=" + balance
				+ ", unsettledInterest=" + unsettledInterest + ", balanceAvailable=" + balanceAvailable + "]";
	}

	public String getWalletType() {
		return walletType;
	}
	
	public String getCurreny() {
		return curreny;
	}

	public double getBalance() {
		return balance;
	}

	public double getUnsettledInterest() {
		return unsettledInterest;
	}

	public double getBalanceAvailable() {
		return balanceAvailable;
	}

}

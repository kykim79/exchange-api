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
package com.ktoy.exchange.api.callback.api;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.Wallet;
import com.google.common.collect.Table;
import org.json.JSONArray;

import java.util.concurrent.CountDownLatch;

public class WalletHandler implements APICallbackHandler {

	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONArray jsonArray)
			throws APIException {
		
		final JSONArray wallets = jsonArray.getJSONArray(2);
		
		// Snapshot or update
		if(! (wallets.get(0) instanceof JSONArray)) {
			handleWalletcallback(apiBroker, wallets);
		} else {
			for(int walletPos = 0; walletPos < wallets.length(); walletPos++) {
				final JSONArray walletArray = wallets.getJSONArray(walletPos);
				handleWalletcallback(apiBroker, walletArray);
			}
		}
		
		notifyLatch(apiBroker);
	}

	/**
	 * Notify the wallet latch
	 * @param apiBroker
	 */
	private void notifyLatch(final ApiBroker apiBroker) {
		
		final CountDownLatch connectionReadyLatch = apiBroker.getConnectionReadyLatch();
		
		if(connectionReadyLatch != null) {
			connectionReadyLatch.countDown();
		}
	}

	/**
	 * Handle the callback for a single wallet
	 * @param apiBroker
	 * @param walletArray
	 * @throws APIException 
	 */
	private void handleWalletcallback(final ApiBroker apiBroker, final JSONArray walletArray) throws APIException {
		final String walletType = walletArray.getString(0);
		final String currency = walletArray.getString(1);
		final double balance = walletArray.getDouble(2);
		final float unsettledInterest = walletArray.getFloat(3);
		final float balanceAvailable = walletArray.optFloat(4, -1);
		
		final Wallet wallet = new Wallet(walletType, currency, balance, unsettledInterest, balanceAvailable);

		final Table<String, String, Wallet> walletTable = apiBroker.getWalletTable();
		
		synchronized (walletTable) {
			walletTable.put(walletType, currency, wallet);
			walletTable.notifyAll();
		}
	}

}

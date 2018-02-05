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
package com.ktoy.exchange.api.test;

import java.util.concurrent.CountDownLatch;

import com.ktoy.exchange.api.BitfinexApiBroker;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.ktoy.exchange.api.callback.api.WalletHandler;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.Wallet;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


public class WalletHandlerTest {

	/**
	 * The delta for double compares
	 */
	private static final double DELTA = 0.001;

	/**
	 * Test the wallet parsing
	 * @throws APIException
	 * @throws InterruptedException 
	 */
	@Test(timeout=20000)
	public void testWalletUpdate() throws APIException, InterruptedException {
		
		final String callbackValue = "[0,\"ws\",[\"exchange\",\"ETH\",9,0,null]]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final CountDownLatch walletLatch = new CountDownLatch(1);
		final Table<String, String, Wallet> walletTable = HashBasedTable.create();

		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getWalletTable()).thenReturn(walletTable);
		Mockito.when(bitfinexApiBroker.getConnectionReadyLatch()).thenReturn(walletLatch);
		
		Assert.assertTrue(walletTable.isEmpty());
		
		final WalletHandler walletHandler = new WalletHandler();
		walletHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		walletLatch.await();
		
		Assert.assertEquals(1, walletTable.size());
		Assert.assertEquals(9, walletTable.get("exchange", "ETH").getBalance(), DELTA);
		Assert.assertEquals(-1, walletTable.get("exchange", "ETH").getBalanceAvailable(), DELTA);
		Assert.assertEquals(0, walletTable.get("exchange", "ETH").getUnsettledInterest(), DELTA);
	}
	
	/**
	 * Test the wallet parsing
	 * @throws APIException
	 * @throws InterruptedException 
	 */
	@Test(timeout=20000)
	public void testWalletSnapshot() throws APIException, InterruptedException {
		
		final String callbackValue = "[0,\"ws\",[[\"exchange\",\"ETH\",9,0,null],[\"exchange\",\"USD\",1826.56468323,0,null],[\"margin\",\"USD\",0,0,null],[\"exchange\",\"XRP\",0,0,null],[\"exchange\",\"EOS\",0,0,null],[\"exchange\",\"NEO\",0,0,null],[\"exchange\",\"LTC\",0,0,null],[\"exchange\",\"IOT\",0,0,null],[\"exchange\",\"BTC\",0,0,null]]]";
		final JSONArray jsonArray = new JSONArray(callbackValue);
		
		final CountDownLatch walletLatch = new CountDownLatch(1);
		final Table<String, String, Wallet> walletTable = HashBasedTable.create();

		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		Mockito.when(bitfinexApiBroker.getWalletTable()).thenReturn(walletTable);
		Mockito.when(bitfinexApiBroker.getConnectionReadyLatch()).thenReturn(walletLatch);
		
		Assert.assertTrue(walletTable.isEmpty());
		
		final WalletHandler walletHandler = new WalletHandler();
		walletHandler.handleChannelData(bitfinexApiBroker, jsonArray);
		walletLatch.await();
		
		Assert.assertEquals(9, walletTable.size());
		
		Assert.assertEquals(9, walletTable.get("exchange", "ETH").getBalance(), DELTA);
		Assert.assertEquals(-1, walletTable.get("exchange", "ETH").getBalanceAvailable(), DELTA);
		Assert.assertEquals(0, walletTable.get("exchange", "ETH").getUnsettledInterest(), DELTA);
		
		Assert.assertEquals(1826.56468323, walletTable.get("exchange", "USD").getBalance(), DELTA);
		Assert.assertEquals(-1, walletTable.get("exchange", "ETH").getBalanceAvailable(), DELTA);
		Assert.assertEquals(0, walletTable.get("exchange", "ETH").getUnsettledInterest(), DELTA);
		
		Assert.assertEquals(0, walletTable.get("margin", "USD").getBalance(), DELTA);
		Assert.assertEquals(-1, walletTable.get("margin", "USD").getBalanceAvailable(), DELTA);
		Assert.assertEquals(0, walletTable.get("margin", "USD").getUnsettledInterest(), DELTA);
		
		Assert.assertTrue(walletTable.get("margin", "USD").toString().length() > 0);
	}
	
}

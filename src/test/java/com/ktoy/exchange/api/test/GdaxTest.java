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

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.GdaxApiBroker;
import com.ktoy.exchange.api.commands.*;
import com.ktoy.exchange.api.entity.*;
import com.ktoy.exchange.api.entity.symbol.*;
import com.ktoy.exchange.api.manager.OrderbookManager;
import com.ktoy.exchange.api.manager.QuoteManager;
import com.ktoy.exchange.api.manager.RawOrderbookManager;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.Tick;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class IntegrationTest {
	
	/**
	 * Try to fetch wallets on an unauthenticated connection
	 */
	@Test
	public void testWalletsOnUnauthClient() throws APIException {
		
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();

		try {
			bitfinexClient.connect();
			Assert.assertFalse(bitfinexClient.isAuthenticated());
			
			try {
				bitfinexClient.getWallets();

				// Should not happen
				Assert.assertTrue(false);
			} catch (APIException e) {
				return;
			}
		
		} catch (Exception e) {
			
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
	
	/**
	 * Test the orderbook stream
	 */
	@Test(timeout=10000)
	public void testOrderbookStream() {
		final BitfinexApiBroker apiBroker = new BitfinexApiBroker();
	
		// Await at least 10 callbacks
		final CountDownLatch latch = new CountDownLatch(10);
		try {
			apiBroker.connect();
			final OrderbookConfiguration orderbookConfiguration = new OrderbookConfiguration(
					BitfinexCurrencyPair.BTC_USD, OrderBookPrecision.P0, OrderBookFrequency.F0, 25);
			
			final OrderbookManager orderbookManager = apiBroker.getOrderbookManager();
			
			final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback = (c, o) -> {
				Assert.assertTrue(o.getAmount() != 0);
				Assert.assertTrue(o.getPrice() != 0);
				Assert.assertTrue(o.getCount() != 0);
				Assert.assertTrue(o.toString().length() > 0);
				latch.countDown();
			};
			
			orderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
			orderbookManager.subscribeOrderbook(new BitfinexSubscribeOrderbookCommand(orderbookConfiguration));
			latch.await();

			AbstractAPICommand apiCommand = new BitfinexUnsubscribeChannelCommand(apiBroker.getChannelForSymbol(BitfinexCurrencyPair.BTC_USD));
			orderbookManager.unsubscribeOrderbook(apiCommand, orderbookConfiguration);
			
			Assert.assertTrue(orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));
			Assert.assertFalse(orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			apiBroker.close();
		}
	}

	/**
	 * Test the orderbook stream
	 */
	@Test//(timeout=10000)
	public void testOrderbookStreamGdax() {
		final GdaxApiBroker apiBroker = new GdaxApiBroker();

		// Await at least 10 callbacks
		final CountDownLatch latch = new CountDownLatch(10);
		try {
			apiBroker.connect();
			final OrderbookConfiguration orderbookConfiguration = new OrderbookConfiguration(
					GdaxCurrencyPair.BTC_USD, OrderBookPrecision.P0, OrderBookFrequency.F0, 25);

			final OrderbookManager orderbookManager = apiBroker.getOrderbookManager();

			final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback = (c, o) -> {
//				Assert.assertTrue(o.getAmount() != 0);
//				Assert.assertTrue(o.getPrice() != 0);
//				Assert.assertTrue(o.getCount() != 0);
//				Assert.assertTrue(o.toString().length() > 0);
				System.out.println(o);
				latch.countDown();
			};

			orderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
			orderbookManager.subscribeOrderbook(new GdaxSubscribeOrderbookCommand(GdaxCurrencyPair.BTC_USD));
			latch.await();

			AbstractAPICommand apiCommand = new GdaxUnsubscribeChannelCommand(GdaxCurrencyPair.BTC_USD);
			orderbookManager.unsubscribeOrderbook(apiCommand, orderbookConfiguration);

			Assert.assertTrue(orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));
			Assert.assertFalse(orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			apiBroker.close();
		}
	}
	
	/**
	 * Test the raw orderbook stream
	 */
	@Test(timeout=10000)
	public void testRawOrderbookStream() {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
	
		// Await at least 20 callbacks
		final CountDownLatch latch = new CountDownLatch(20);
		try {
			bitfinexClient.connect();
			final RawOrderbookConfiguration orderbookConfiguration = new RawOrderbookConfiguration(
					BitfinexCurrencyPair.BTC_USD);
			
			final RawOrderbookManager rawOrderbookManager = bitfinexClient.getRawOrderbookManager();
			
			final BiConsumer<RawOrderbookConfiguration, RawOrderbookEntry> callback = (c, o) -> {
				Assert.assertTrue(o.getAmount() != 0);
				Assert.assertTrue(o.getPrice() != 0);
				Assert.assertTrue(o.getOrderId() >= 0);
				Assert.assertTrue(o.toString().length() > 0);
				latch.countDown();
			};
			
			rawOrderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
			rawOrderbookManager.subscribeOrderbook(orderbookConfiguration);
			latch.await();

			rawOrderbookManager.unsubscribeOrderbook(orderbookConfiguration);
			
			Assert.assertTrue(rawOrderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));
			Assert.assertFalse(rawOrderbookManager.removeOrderbookCallback(orderbookConfiguration, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}
	
	/**
	 * Test the candle stream
	 */
	@Test(timeout=10000)
	public void testCandleStream() {
		final ApiBroker apiBroker = new BitfinexApiBroker();

		// Await at least 10 callbacks
		final CountDownLatch latch = new CountDownLatch(10);
		try {
			apiBroker.connect();
			final ChannelSymbol symbol = new BitfinexCandlestickSymbol(
					BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_1);

			final QuoteManager orderbookManager = apiBroker.getQuoteManager();

			final BiConsumer<ChannelSymbol, Tick> callback = (c, o) -> {
				latch.countDown();
			};

			orderbookManager.registerCandlestickCallback(symbol, callback);

			AbstractAPICommand apiCommand = new BitfinexSubscribeCandlesCommand(symbol);
			orderbookManager.subscribeCandles(apiCommand);
			latch.await();

			orderbookManager.unsubscribeCandles(apiCommand, symbol);

			Assert.assertTrue(orderbookManager.removeCandlestickCallback(symbol, callback));
			Assert.assertFalse(orderbookManager.removeCandlestickCallback(symbol, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			apiBroker.close();
		}
	}
	
	/**
	 * Test the tick stream
	 */
	@Test(timeout=10000)
	public void testTickerStream() {
		final ApiBroker bitfinexClient = new BitfinexApiBroker();
	
		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(10);
		try {
			bitfinexClient.connect();
			//final ChannelSymbol symbol = BitfinexCurrencyPair.BTC_USD;
			final ChannelSymbol symbol = BitfinexCurrencyPair.BCH_USD;
			
			final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();
			
			final BiConsumer<ChannelSymbol, Tick> callback = (c, o) -> {
				System.out.println(o);
				latch.countDown();
			};
			
			orderbookManager.registerTickCallback(symbol, callback);
			AbstractAPICommand apiCommand = new BitfinexSubscribeTickerCommand(symbol);
			orderbookManager.subscribeTicker(apiCommand);
			latch.await();
			Assert.assertTrue(bitfinexClient.isTickerActive(symbol));

			orderbookManager.unsubscribeTicker(apiCommand, symbol);
			Assert.assertFalse(bitfinexClient.isTickerActive(symbol));

			Assert.assertTrue(orderbookManager.removeTickCallback(symbol, callback));
			Assert.assertFalse(orderbookManager.removeTickCallback(symbol, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			bitfinexClient.close();
		}
	}

	@Test(timeout=50000)
	public void testTickerStreamGDax() {
		final ApiBroker client = new GdaxApiBroker();

		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(50);
		try {
			client.connect();
			final ChannelSymbol symbol = GdaxCurrencyPair.BTC_USD;

			final QuoteManager orderbookManager = client.getQuoteManager();

			final BiConsumer<ChannelSymbol, Tick> callback = (c, o) -> {
				System.out.println(o);
				latch.countDown();
			};

			orderbookManager.registerTickCallback(symbol, callback);

			AbstractAPICommand apiCommand = new GdaxSubscribeTickerCommand(symbol);
			orderbookManager.subscribeTicker(apiCommand);
			latch.await();

			Assert.assertTrue(client.isTickerActive(symbol));

			orderbookManager.unsubscribeTicker(apiCommand, symbol);
			Assert.assertFalse(client.isTickerActive(symbol));

			Assert.assertTrue(orderbookManager.removeTickCallback(symbol, callback));
			Assert.assertFalse(orderbookManager.removeTickCallback(symbol, callback));

		} catch (Exception e) {
			// Should not happen
			e.printStackTrace();
			Assert.assertTrue(false);
		} finally {
			client.close();
		}
	}

	/**
	 * Test auth failed
	 * @throws APIException 
	 */
	@Test(expected=APIException.class, timeout=10000)
	public void testAuthFailed() throws APIException {
		final String KEY = "key";
		final String SECRET = "secret";
		
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker(KEY, SECRET);
		Assert.assertEquals(KEY, bitfinexClient.getApiKey());
		Assert.assertEquals(SECRET, bitfinexClient.getApiSecret());
		
		Assert.assertFalse(bitfinexClient.isAuthenticated());
		
		bitfinexClient.connect();
		
		// Should not be reached
		Assert.assertTrue(false);
		bitfinexClient.close();
	}
	
	/**
	 * Test the session reconnect
	 * @throws APIException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testReconnect() throws APIException, InterruptedException {
		final BitfinexApiBroker bitfinexClient = new BitfinexApiBroker();
		bitfinexClient.connect();
		
		final ChannelSymbol symbol = BitfinexCurrencyPair.BTC_USD;
		final QuoteManager orderbookManager = bitfinexClient.getQuoteManager();

		AbstractAPICommand apiCommand = new BitfinexSubscribeTickerCommand(symbol);
		orderbookManager.subscribeTicker(apiCommand);
		Thread.sleep(1000);
		bitfinexClient.reconnect();
		
		// Await at least 2 callbacks
		final CountDownLatch latch = new CountDownLatch(2);
		
		final BiConsumer<ChannelSymbol, Tick> callback = (c, o) -> {
			latch.countDown();
		};
		
		orderbookManager.registerTickCallback(symbol, callback);
		latch.await();
		Assert.assertTrue(bitfinexClient.isTickerActive(symbol));

		orderbookManager.unsubscribeTicker(apiCommand, symbol);
		Assert.assertFalse(bitfinexClient.isTickerActive(symbol));
		
		bitfinexClient.close();
	}
}

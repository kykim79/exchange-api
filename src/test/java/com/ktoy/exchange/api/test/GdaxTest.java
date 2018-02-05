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

import com.ktoy.exchange.api.GdaxApiBroker;
import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.commands.GdaxSubscribeOrderbookCommand;
import com.ktoy.exchange.api.commands.GdaxSubscribeTickerCommand;
import com.ktoy.exchange.api.commands.GdaxUnsubscribeChannelCommand;
import com.ktoy.exchange.api.entity.OrderBookFrequency;
import com.ktoy.exchange.api.entity.OrderBookPrecision;
import com.ktoy.exchange.api.entity.OrderbookConfiguration;
import com.ktoy.exchange.api.entity.OrderbookEntry;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import com.ktoy.exchange.api.entity.symbol.GdaxCurrencyPair;
import com.ktoy.exchange.api.manager.OrderbookManager;
import com.ktoy.exchange.api.manager.QuoteManager;
import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.Tick;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class GdaxTest {

    /**
     * Test the orderbook stream
     */
    @Test//(timeout=10000)
    public void testOrderbookStream() {

        // Await at least 10 callbacks
        final CountDownLatch latch = new CountDownLatch(10);
        try (GdaxApiBroker apiBroker = new GdaxApiBroker()) {
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
        }
    }

    @Test//(timeout=50000)
    public void testTickerStream() {
        // Await at least 2 callbacks
        final CountDownLatch latch = new CountDownLatch(50);
        try (GdaxApiBroker client = new GdaxApiBroker()) {
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
        }
    }
}

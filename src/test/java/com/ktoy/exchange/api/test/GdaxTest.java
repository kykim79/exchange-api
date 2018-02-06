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

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.GdaxApiBroker;
import com.ktoy.exchange.api.commands.*;
import com.ktoy.exchange.api.entity.*;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
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
    @Test
    public void testOrderbookStream() {

        // Await at least 10 callbacks
        final CountDownLatch latch = new CountDownLatch(10);
        try (GdaxApiBroker apiBroker = new GdaxApiBroker()) {
            apiBroker.connect();
            final OrderbookConfiguration orderbookConfiguration = new OrderbookConfiguration(
                    GdaxCurrencyPair.BTC_USD, OrderBookPrecision.P0, OrderBookFrequency.F0, 25);

            final OrderbookManager orderbookManager = apiBroker.getOrderbookManager();

            final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback = (c, o) -> {
                System.out.println(o);
                latch.countDown();
            };

            orderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
            orderbookManager.subscribeOrderbook(new GdaxSubscribeOrderbookCommand(GdaxCurrencyPair.BTC_USD));
            latch.await();

            AbstractAPICommand apiCommand = new GdaxUnsubscribeChannelCommand(GdaxCurrencyPair.BTC_USD);
            orderbookManager.unsubscribeOrderbook(apiCommand, orderbookConfiguration);
            orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback);
        } catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }
    }

    @Test
    public void testTradeStream() {

//        // Await at least 10 callbacks
//        final CountDownLatch latch = new CountDownLatch(10);
//        try (GdaxApiBroker apiBroker = new GdaxApiBroker()) {
//            apiBroker.connect();
//            final OrderbookConfiguration orderbookConfiguration = new OrderbookConfiguration(
//                    GdaxCurrencyPair.BTC_USD, OrderBookPrecision.P0, OrderBookFrequency.F0, 25);
//
//            final OrderbookManager orderbookManager = apiBroker.getOrderbookManager();
//
//            final BiConsumer<OrderbookConfiguration, Trade> callback = (c, o) -> {
//                System.out.println(o);
//                latch.countDown();
//            };
//
//            orderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
//            orderbookManager.subscribeOrderbook(new GdaxSubscribeFullCommand(GdaxCurrencyPair.BTC_USD));
//            latch.await();
//
//            AbstractAPICommand apiCommand = new GdaxUnsubscribeChannelCommand(GdaxCurrencyPair.BTC_USD);
//            orderbookManager.unsubscribeOrderbook(apiCommand, orderbookConfiguration);
//            orderbookManager.removeOrderbookCallback(orderbookConfiguration, callback);
//        } catch (Exception e) {
//            // Should not happen
//            e.printStackTrace();
//        }


        // Await at least 2 callbacks
        final CountDownLatch latch = new CountDownLatch(10);
        try (GdaxApiBroker apiBroker = new GdaxApiBroker()){
            apiBroker.connect();
            //final ChannelSymbol symbol = BitfinexCurrencyPair.BTC_USD;
            final ChannelSymbol symbol = GdaxCurrencyPair.BTC_USD;

            final QuoteManager orderbookManager = apiBroker.getQuoteManager();

            final BiConsumer<ChannelSymbol, Trade> callback = (c, o) -> {
                System.out.println(o);
                //latch.countDown();
            };

            orderbookManager.registerTradeCallback(symbol, callback);
            AbstractAPICommand apiCommand = new GdaxSubscribeFullCommand(symbol);
            orderbookManager.subscribe(apiCommand);
            latch.await();

            orderbookManager.unsubscribe(apiCommand, symbol);
            orderbookManager.removeTradeCallback(symbol, callback);

        } catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }
    }

    @Test
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
            orderbookManager.subscribe(apiCommand);
            latch.await();

            Assert.assertTrue(client.isTickerActive(symbol));

            orderbookManager.unsubscribe(apiCommand, symbol);
            orderbookManager.removeTickCallback(symbol, callback);


        } catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }
    }
}

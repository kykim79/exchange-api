package com.ktoy.exchange.api.test;

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.commands.BitfinexSubscribeOrderbookCommand;
import com.ktoy.exchange.api.commands.BitfinexUnsubscribeChannelCommand;
import com.ktoy.exchange.api.entity.OrderBookFrequency;
import com.ktoy.exchange.api.entity.OrderBookPrecision;
import com.ktoy.exchange.api.entity.OrderbookConfiguration;
import com.ktoy.exchange.api.entity.OrderbookEntry;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.manager.OrderbookManager;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class BitfinexOrderbookExample {
    public static void main(String[] args) {

        // Await at least 10 callbacks
        final CountDownLatch latch = new CountDownLatch(10);
        try (BitfinexApiBroker apiBroker = new BitfinexApiBroker()){
            apiBroker.connect();
            final OrderbookConfiguration orderbookConfiguration = new OrderbookConfiguration(
                    BitfinexCurrencyPair.BTC_USD, OrderBookPrecision.P0, OrderBookFrequency.F0, 25);

            final OrderbookManager orderbookManager = apiBroker.getOrderbookManager();

            final BiConsumer<OrderbookConfiguration, OrderbookEntry> callback = (c, o) -> {
                System.out.println(o);
                //latch.countDown();
            };

            orderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
            orderbookManager.subscribeOrderbook(new BitfinexSubscribeOrderbookCommand(orderbookConfiguration));
            latch.await();

            AbstractAPICommand apiCommand = new BitfinexUnsubscribeChannelCommand(apiBroker.getChannelForSymbol(BitfinexCurrencyPair.BTC_USD));
            orderbookManager.unsubscribeOrderbook(apiCommand, orderbookConfiguration);
        } catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }
    }
}

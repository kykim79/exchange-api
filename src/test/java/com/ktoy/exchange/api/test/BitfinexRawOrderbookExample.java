package com.ktoy.exchange.api.test;

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.entity.RawOrderbookConfiguration;
import com.ktoy.exchange.api.entity.RawOrderbookEntry;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.manager.RawOrderbookManager;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class BitfinexRawOrderbookExample {
    public static void main(String[] args) {

        // Await at least 20 callbacks
        final CountDownLatch latch = new CountDownLatch(20);
        try (BitfinexApiBroker apiBroker = new BitfinexApiBroker()){
            apiBroker.connect();
            final RawOrderbookConfiguration orderbookConfiguration = new RawOrderbookConfiguration(
                    BitfinexCurrencyPair.BTC_USD);

            final RawOrderbookManager rawOrderbookManager = apiBroker.getRawOrderbookManager();

            final BiConsumer<RawOrderbookConfiguration, RawOrderbookEntry> callback = (c, o) -> {
                System.out.println(o);
                //latch.countDown();
            };

            rawOrderbookManager.registerOrderbookCallback(orderbookConfiguration, callback);
            rawOrderbookManager.subscribeOrderbook(orderbookConfiguration);
            latch.await();

            rawOrderbookManager.unsubscribeOrderbook(orderbookConfiguration);
            rawOrderbookManager.removeOrderbookCallback(orderbookConfiguration, callback);
            rawOrderbookManager.removeOrderbookCallback(orderbookConfiguration, callback);
        } catch (Exception e) {
            // Should not happen
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }
}

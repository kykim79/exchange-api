package com.ktoy.exchange.api.test;

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.commands.BitfinexSubscribeCandlesCommand;
import com.ktoy.exchange.api.entity.*;
import com.ktoy.exchange.api.entity.symbol.BitfinexCandlestickSymbol;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import com.ktoy.exchange.api.manager.QuoteManager;
import org.ta4j.core.Tick;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class BitfinexCandleExample {
    public static void main(String[] args) {

        // Await at least 10 callbacks
        final CountDownLatch latch = new CountDownLatch(10);
        try (BitfinexApiBroker apiBroker = new BitfinexApiBroker()){
            apiBroker.connect();
            final ChannelSymbol symbol = new BitfinexCandlestickSymbol(
                    BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_1);

            final QuoteManager orderbookManager = apiBroker.getQuoteManager();

            final BiConsumer<ChannelSymbol, Tick> callback = (c, o) -> {
                System.out.println(o);
                //latch.countDown();
            };

            orderbookManager.registerCandlestickCallback(symbol, callback);
            AbstractAPICommand apiCommand = new BitfinexSubscribeCandlesCommand(symbol);
            orderbookManager.subscribe(apiCommand);
            latch.await();

            orderbookManager.unsubscribe(apiCommand, symbol);
        } catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }
    }
}

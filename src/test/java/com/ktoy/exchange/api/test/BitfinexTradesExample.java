package com.ktoy.exchange.api.test;

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.commands.BitfinexSubscribeTickerCommand;
import com.ktoy.exchange.api.commands.BitfinexSubscribeTradeCommand;
import com.ktoy.exchange.api.entity.Trade;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import com.ktoy.exchange.api.manager.QuoteManager;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class BitfinexTradesExample {
    public static void main(String[] args) {

        // Await at least 2 callbacks
        final CountDownLatch latch = new CountDownLatch(10);
        try (BitfinexApiBroker apiBroker = new BitfinexApiBroker()){
            apiBroker.connect();
            //final ChannelSymbol symbol = BitfinexCurrencyPair.BTC_USD;
            final ChannelSymbol symbol = BitfinexCurrencyPair.BTC_USD;

            final QuoteManager orderbookManager = apiBroker.getQuoteManager();

            final BiConsumer<ChannelSymbol, Trade> callback = (c, o) -> {
                System.out.println(o);
                //latch.countDown();
            };

            orderbookManager.registerTradeCallback(symbol, callback);
            AbstractAPICommand apiCommand = new BitfinexSubscribeTradeCommand(symbol);
            orderbookManager.subscribe(apiCommand);
            latch.await();

            orderbookManager.unsubscribe(apiCommand, symbol);
            orderbookManager.removeTradeCallback(symbol, callback);

        } catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }
    }
}

package com.ktoy.exchange.api.test;

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.commands.BitfinexSubscribeTickerCommand;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import com.ktoy.exchange.api.manager.QuoteManager;
import org.ta4j.core.Tick;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

public class BitfinexTickerExample {
    public static void main(String[] args) {

        // Await at least 2 callbacks
        final CountDownLatch latch = new CountDownLatch(10);
        try (BitfinexApiBroker apiBroker = new BitfinexApiBroker()){
            apiBroker.connect();
            //final ChannelSymbol symbol = BitfinexCurrencyPair.BTC_USD;
            final ChannelSymbol symbol = BitfinexCurrencyPair.BTC_USD;


            apiBroker.getTradeManager().registerCallback((t) -> {
                System.out.println(t);
            });
            final QuoteManager orderbookManager = apiBroker.getQuoteManager();

//            final BiConsumer<ChannelSymbol, Tick> callback = (c, o) -> {
//                System.out.println(o);
//                //latch.countDown();
//            };

            //orderbookManager.registerTickCallback(symbol, callback);
            AbstractAPICommand apiCommand = new BitfinexSubscribeTickerCommand(symbol);
            orderbookManager.subscribe(apiCommand);
            latch.await();

            orderbookManager.unsubscribe(apiCommand, symbol);
            //orderbookManager.removeTickCallback(symbol, callback);

        } catch (Exception e) {
            // Should not happen
            e.printStackTrace();
        }
    }
}

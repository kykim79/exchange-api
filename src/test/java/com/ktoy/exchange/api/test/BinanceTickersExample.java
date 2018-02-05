package com.ktoy.exchange.api.test;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.CandlestickInterval;

/**
 * All market tickers channel examples.
 * <p>
 * It illustrates how to create a stream to obtain all market tickers.
 */
public class BinanceTickersExample {

    public static void main(String[] args) {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
        BinanceApiWebSocketClient client = factory.newWebSocketClient();

        client.onCandlestickEvent("BTCUSDT".toLowerCase(), CandlestickInterval.ONE_MINUTE, e -> {
            System.out.println(e);
        });

        client.onAggTradeEvent("ETHBTC".toLowerCase(), e -> {
            System.out.println(e);
        });

//        client.onAllMarketTickersEvent(event -> {
//            System.out.println(event);
//        });
    }
}
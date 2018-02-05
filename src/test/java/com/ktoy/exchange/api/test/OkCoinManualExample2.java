package com.ktoy.exchange.api.test;

import com.cf.client.WSSClient;
import com.cf.data.handler.poloniex.PoloniexSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.poloniex2.PoloniexStreamingExchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.wamp.jawampa.ApplicationError;

public class PoloniexManualExample2 {
    private static final Logger LOG = LoggerFactory.getLogger(PoloniexManualExample2.class);

    public static void main(String[] args) {
        try (WSSClient wssClient = new WSSClient("wss://api.poloniex.com", "realm1")) {
            wssClient.subscribe(PoloniexSubscription.TICKER);
            //wssClient.subscribe(new PoloniexSubscription("USDT_BTC"));
            wssClient.subscribe(new PoloniexSubscription("BTC_XMR"));
            wssClient.run(60000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

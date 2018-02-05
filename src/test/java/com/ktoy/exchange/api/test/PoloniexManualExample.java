package com.ktoy.exchange.api.test;

import com.cf.client.WSSClient;
import com.cf.data.handler.poloniex.PoloniexSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David
 */
public class PoloniexWSSClientExample
{
    private final static Logger LOGGER = LoggerFactory.getLogger(PoloniexWSSClientExample.class);
    private static final String ENDPOINT_URL = "wss://api.poloniex.com";
    private static final String DEFAULT_REALM = "realm1";

    public static void main(String[] args)
    {
        try {
            new PoloniexWSSClientExample().run();

            //Thread.sleep(50000);
        } catch (Exception ex) {
            LOGGER.error("An exception occurred when running PoloniexWSSClientExample - {}", ex.getMessage());
            System.exit(-1);
        }
    }

    public void run() throws Exception
    {
//        try (WSSClient wssClient = new WSSClient(ENDPOINT_URL, DEFAULT_REALM)) {
//            wssClient.subscribe(PoloniexSubscription.TICKER);
//            wssClient.run(60000);
//        }

        try (WSSClient poloniexWSSClient = new WSSClient(ENDPOINT_URL, DEFAULT_REALM)) {
            poloniexWSSClient.subscribe(PoloniexSubscription.TICKER);
            poloniexWSSClient.subscribe(new PoloniexSubscription("USDT_BTC"));
            poloniexWSSClient.run(60000);
        }
    }
}

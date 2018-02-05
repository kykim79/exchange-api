package com.ktoy.exchange.api.test;

import com.cf.client.WSSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;
import ws.wamp.jawampa.PubSubData;

public class OkCoinManualExample2 {
    private static final Logger LOG = LoggerFactory.getLogger(OkCoinManualExample2.class);

    public static void main(String[] args) {
        try (WSSClient wssClient = new WSSClient("wss://real.okcoin.com:10440/websocket", "realm1")) {
            wssClient.subscribe("ticker", Subscription.TICKER);
            //wssClient.subscribe(new Subscription("USDT_BTC"));
            wssClient.subscribe("BTC_XML", new Subscription("BTC_XMR"));
            wssClient.run(60000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class Subscription implements Action1<PubSubData> {
        public static final Subscription TICKER = new Subscription("ticker");

        protected final Logger LOG = LoggerFactory.getLogger(Subscription.class);
        public final String feedName;

        public Subscription(String feedName) {
            this.feedName = feedName;
        }

        @Override
        public void call(PubSubData event) {
            try {
                LOG.trace("{}", event.arguments());
            } catch (Exception ex) {
                LOG.warn("Exception processing event data - " + ex.getMessage());
            }
        }
    }
}

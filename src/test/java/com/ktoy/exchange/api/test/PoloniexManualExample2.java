package com.ktoy.exchange.api.test;


import com.cf.client.WSSClient;
import com.cf.data.handler.poloniex.PoloniexSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoloniexManualExample2 {
    private static final Logger LOG = LoggerFactory.getLogger(PoloniexManualExample2.class);

    public static void main(String[] args) {
        try (WSSClient wssClient = new WSSClient("wss://api.poloniex.com", "realm1")) {
            wssClient.subscribe(PoloniexSubscription.TICKER);
            //wssClient.subscribe(new PoloniexSubscription("BTC_USDT"));
            //wssClient.subscribe("BTC_XML", new Subscription("BTC_XMR"));
            wssClient.run(60000);

            //Thread.sleep(100000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    static class Subscription implements Action1<PubSubData> {
//        public static final Subscription TICKER = new Subscription("ticker");
//
//        protected final Logger LOG = LoggerFactory.getLogger(Subscription.class);
//        public final String feedName;
//
//        public Subscription(String feedName) {
//            this.feedName = feedName;
//        }
//
//        @Override
//        public void call(PubSubData event) {
//            try {
//                LOG.trace("{}", event.arguments());
//            } catch (Exception ex) {
//                LOG.warn("Exception processing event data - " + ex.getMessage());
//            }
//        }
//    }
}

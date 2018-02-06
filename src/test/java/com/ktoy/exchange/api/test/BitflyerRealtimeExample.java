package com.ktoy.exchange.api.test;

import com.after_sunrise.cryptocurrency.bitflyer4j.Bitflyer4j;
import com.after_sunrise.cryptocurrency.bitflyer4j.Bitflyer4jFactory;
import com.after_sunrise.cryptocurrency.bitflyer4j.entity.Board;
import com.after_sunrise.cryptocurrency.bitflyer4j.entity.Execution;
import com.after_sunrise.cryptocurrency.bitflyer4j.entity.Tick;
import com.after_sunrise.cryptocurrency.bitflyer4j.service.RealtimeListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BitflyerRealtimeExample {

    public static void main(String[] args) throws Exception {

        Bitflyer4j api = new Bitflyer4jFactory().createInstance();

        api.getRealtimeService().addListener(new RealtimeListener.RealtimeListenerAdapter() {

//            @Override
//            public void onBoards(String product, Board value) {
//                System.out.println(String.format("product:%s, board: %s", product, value));
//            }
//
//            @Override
//            public void onTicks(String product, List<Tick> values) {
//                values.forEach(e -> System.out.println("(" + product + ")" + e));
//            }

            @Override
            public void onExecutions(String product, List<Execution> values) {
                values.forEach(e -> System.out.println(String.format("[%s] time=%s, id=%s, side=%s, price=%s, size=%s", product, e.getTimestamp(), e.getId(), e.getSide(), e.getPrice(), e.getSize())));
            }
        });


        System.out.println(api.getRealtimeService().subscribeBoard(Arrays.asList("BTC_JPY")).get());

        System.out.println(api.getRealtimeService().subscribeTick(Arrays.asList("BTC_JPY")).get());

        System.out.println(api.getRealtimeService().subscribeExecution(Arrays.asList("BTC_JPY")).get());

        TimeUnit.SECONDS.sleep(1000);

        api.close();

    }

}

package com.ktoy.exchange.api.test;

import com.after_sunrise.cryptocurrency.bitflyer4j.Bitflyer4j;
import com.after_sunrise.cryptocurrency.bitflyer4j.Bitflyer4jFactory;
import com.after_sunrise.cryptocurrency.bitflyer4j.entity.Tick;
import com.after_sunrise.cryptocurrency.bitflyer4j.service.RealtimeListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BitflyerRealtimeExample {

    public static void main(String[] args) throws Exception {

        Bitflyer4j api = new Bitflyer4jFactory().createInstance();

        api.getRealtimeService().addListener(new RealtimeListener.RealtimeListenerAdapter() {
            @Override
            public void onTicks(String product, List<Tick> values) {
                System.out.println("(" + product + ")" + values);
            }
        });

        System.out.println(api.getRealtimeService().subscribeTick(Arrays.asList("BTC_JPY")).get());

        TimeUnit.SECONDS.sleep(30L);

        api.close();

    }

}

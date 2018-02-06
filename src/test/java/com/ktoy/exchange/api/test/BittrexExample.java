package com.ktoy.exchange.api.test;

import com.github.ccob.bittrex4j.BittrexExchange;
import com.github.ccob.bittrex4j.dao.Fill;
import com.github.ccob.bittrex4j.dao.MarketOrder;
import com.github.ccob.bittrex4j.dao.UpdateExchangeState;

import java.io.IOException;
import java.util.Arrays;

public class BittrexExample {

    @FunctionalInterface
    interface Function3<One, Two, Three> {
        public void apply(One one, Two two, Three three);
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Press any key to quit");

        try (BittrexExchange bittrexExchange = new BittrexExchange()) {
            bittrexExchange.onUpdateSummaryState(exchangeSummaryState -> {
                if (exchangeSummaryState.getDeltas().length > 0) {

                    Arrays.stream(exchangeSummaryState.getDeltas())
                            .filter(marketSummary -> marketSummary.getMarketName().equals("USDT-BTC"))
                            .forEach(marketSummary -> System.out.println(
                                    String.format("[%s] last price %s. 24 hour volume for market %s: %s",
                                            marketSummary.getTimeStamp().toString(),
                                            marketSummary.getLast().toString(),
                                            marketSummary.getMarketName(),
                                            marketSummary.getVolume().toString())));
                }
            });

            Function3<String, MarketOrder[], UpdateExchangeState> orderfunc = (type, orders, state) ->
                    Arrays.stream(orders).forEach(e -> System.out.println(String.format("[%s, %s] type: %d, price: %s, quantity: %s",
                            state.getMarketName(), type, e.getType(), e.getRate().toString(), e.getQuantity().toString())));

            Function3<String, Fill[], UpdateExchangeState> fillFunc = (type, fills, state) ->
                    Arrays.stream(fills).forEach(e -> System.out.println(String.format("%s [%s, %s] price: %f, quantity: %f",
                            e.getTimeStamp(), state.getMarketName(), e.getOrderType(), e.getPrice(), e.getQuantity())));

            bittrexExchange.onUpdateExchangeState((UpdateExchangeState updateExchangeState) -> {
//                double volume = Arrays.stream(updateExchangeState.getFills())
//                        .mapToDouble(Fill::getQuantity)
//                        .sum();

                fillFunc.apply("fill", updateExchangeState.getFills(), updateExchangeState);
//                orderfunc.apply("buy order", updateExchangeState.getBuys(), updateExchangeState);
//                orderfunc.apply("sell order", updateExchangeState.getSells(), updateExchangeState);
            });

            bittrexExchange.connectToWebSocket(() -> {
                bittrexExchange.subscribeToExchangeDeltas("USDT-BTC", null);
//                bittrexExchange.subscribeToExchangeDeltas("BTC-BCC", null);
                //bittrexExchange.subscribeToMarketSummaries(null);
            });

            System.in.read();
        }

        System.out.println("Closing websocket and exiting");
    }
}

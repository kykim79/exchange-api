/*******************************************************************************
 *
 *    Copyright (C) 2018 Kim Kwon Young
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License. 
 *
 *******************************************************************************/
package com.ktoy.exchange.api.test;

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.callback.channel.BitfinexCandlestickHandler;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.Timeframe;
import com.ktoy.exchange.api.entity.symbol.BitfinexCandlestickSymbol;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import com.ktoy.exchange.api.manager.QuoteManager;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class CandlestickHandlerTest {

    /**
     * The delta for double compares
     */
    private static final double DELTA = 0.001;

    /**
     * Test the parsing of one candlestick
     *
     * @throws APIException
     */
    @Test
    public void testCandlestickUpdateAndNotify() throws APIException {

        final String callbackValue = "[15134900000,15996,15997,16000,15980,318.5139342]";
        final JSONArray jsonArray = new JSONArray(callbackValue);

        final BitfinexCandlestickSymbol symbol
                = new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_1);

        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
        Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
        final QuoteManager tickerManager = new QuoteManager(bitfinexApiBroker);
        Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(tickerManager);

        final AtomicInteger counter = new AtomicInteger(0);

        tickerManager.registerCandlestickCallback(symbol, (s, c) -> {
            counter.incrementAndGet();
            Assert.assertEquals(symbol, s);
            Assert.assertEquals(15996, c.getOpenPrice().toDouble(), DELTA);
            Assert.assertEquals(15997, c.getClosePrice().toDouble(), DELTA);
            Assert.assertEquals(16000, c.getMaxPrice().toDouble(), DELTA);
            Assert.assertEquals(15980, c.getMinPrice().toDouble(), DELTA);
            Assert.assertEquals(318.5139342, c.getVolume().toDouble(), DELTA);
        });

        final BitfinexCandlestickHandler candlestickHandler = new BitfinexCandlestickHandler();
        candlestickHandler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);

        Assert.assertEquals(1, counter.get());
    }


    /**
     * Test the parsing of a candlestick snapshot
     *
     * @throws APIException
     */
    @Test
    public void testCandlestickSnapshotUpdateAndNotify() throws APIException {

        final String callbackValue = "[[15134900000,15996,15997,16000,15980,318.5139342],[15135100000,15899,15996,16097,15890,1137.180342268]]";
        final JSONArray jsonArray = new JSONArray(callbackValue);

        final BitfinexCandlestickSymbol symbol
                = new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_1);

        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
        Mockito.when(bitfinexApiBroker.getExecutorService()).thenReturn(executorService);
        final QuoteManager tickerManager = new QuoteManager(bitfinexApiBroker);
        Mockito.when(bitfinexApiBroker.getQuoteManager()).thenReturn(tickerManager);

        final AtomicInteger counter = new AtomicInteger(0);

        tickerManager.registerCandlestickCallback(symbol, (s, c) -> {
            Assert.assertEquals(symbol, s);
            final int counterValue = counter.getAndIncrement();
            if (counterValue == 0) {
                Assert.assertEquals(15996, c.getOpenPrice().toDouble(), DELTA);
                Assert.assertEquals(15997, c.getClosePrice().toDouble(), DELTA);
                Assert.assertEquals(16000, c.getMaxPrice().toDouble(), DELTA);
                Assert.assertEquals(15980, c.getMinPrice().toDouble(), DELTA);
                Assert.assertEquals(318.5139342, c.getVolume().toDouble(), DELTA);
            } else if (counterValue == 1) {
                Assert.assertEquals(15899, c.getOpenPrice().toDouble(), DELTA);
                Assert.assertEquals(15996, c.getClosePrice().toDouble(), DELTA);
                Assert.assertEquals(16097, c.getMaxPrice().toDouble(), DELTA);
                Assert.assertEquals(15890, c.getMinPrice().toDouble(), DELTA);
                Assert.assertEquals(1137.180342268, c.getVolume().toDouble(), DELTA);
            } else {
                throw new IllegalArgumentException("Illegal call, expected 2 candlesticks");
            }
        });

        final BitfinexCandlestickHandler candlestickHandler = new BitfinexCandlestickHandler();
        candlestickHandler.handleChannelData(bitfinexApiBroker, symbol, jsonArray);

        Assert.assertEquals(2, counter.get());
    }

    /**
     * Test the symbol encoding and decoding
     */
    @Test
    public void testCandlestickSymbolEncoding1() {
        final BitfinexCandlestickSymbol symbol1
                = new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BCH_USD, Timeframe.MINUTES_15);

        final BitfinexCandlestickSymbol symbol2
                = new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BTC_USD, Timeframe.MINUTES_15);

        Assert.assertFalse(symbol1.equals(symbol2));

        final String symbol1String = symbol1.toSymbolString();
        final String symbol2String = symbol2.toSymbolString();

        Assert.assertEquals(symbol1, BitfinexCandlestickSymbol.fromString(symbol1String));
        Assert.assertEquals(symbol2, BitfinexCandlestickSymbol.fromString(symbol2String));
    }

    /**
     * Test the symbol encoding and decoding
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCandlestickSymbolEncoding2() {
        final String symbol = "dffdsf:dfsfd:dsdfd";
        BitfinexCandlestickSymbol.fromString(symbol);
    }

    /**
     * Test the symbol encoding and decoding
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCandlestickSymbolEncoding3() {
        final String symbol = "trading:";
        BitfinexCandlestickSymbol.fromString(symbol);
    }
}

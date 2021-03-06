package com.ktoy.exchange.api.test;

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.BitfinexOrderBuilder;
import com.ktoy.exchange.api.commands.*;
import com.ktoy.exchange.api.entity.*;
import com.ktoy.exchange.api.entity.symbol.BitfinexCandlestickSymbol;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

public class CommandsTest {

    /**
     * Call all commands and check for excepion
     *
     * @throws CommandException
     */
    @Test
    public void testCommandsJSON() throws CommandException {

        final BitfinexOrder order
                = BitfinexOrderBuilder.create(
                BitfinexCurrencyPair.BCH_USD, BitfinexOrderType.EXCHANGE_STOP, 2).build();

        final BitfinexCandlestickSymbol candleSymbol
                = new BitfinexCandlestickSymbol(BitfinexCurrencyPair.BCH_USD, Timeframe.HOUR_1);

        final OrderbookConfiguration orderbookConfiguration
                = new OrderbookConfiguration(BitfinexCurrencyPair.BCH_USD,
                OrderBookPrecision.P0, OrderBookFrequency.F0, 50);

        final RawOrderbookConfiguration rawOrderbookConfiguration
                = new RawOrderbookConfiguration(BitfinexCurrencyPair.BAT_BTC);

        final List<AbstractAPICommand> commands = Arrays.asList(
                new BitfinexAuthCommand(),
                new BitfinexCancelOrderCommand(123),
                new BitfinexCancelOrderGroupCommand(1),
                new BitfinexOrderCommand(order),
                new BitfinexPingCommand(),
                new BitfinexSubscribeCandlesCommand(candleSymbol),
                new BitfinexSubscribeTickerCommand(BitfinexCurrencyPair.BCH_USD),
                new BitfinexSubscribeOrderbookCommand(orderbookConfiguration),
                new BitfinexSubscribeRawOrderbookCommand(rawOrderbookConfiguration),
                new BitfinexUnsubscribeChannelCommand(12));

        final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();

        for (final AbstractAPICommand command : commands) {
            final String commandValue = command.getCommand(bitfinexApiBroker);
            Assert.assertNotNull(commandValue);
            Assert.assertTrue(commandValue.length() > 10);
        }
    }

    /**
     * Test the order command
     *
     * @throws CommandException
     */
    @Test
    public void testOrderCommand() throws CommandException {
        final BitfinexOrder order
                = BitfinexOrderBuilder.create(BitfinexCurrencyPair.BCH_USD, BitfinexOrderType.EXCHANGE_STOP, 2)
                .setHidden()
                .setPostOnly()
                .withPrice(12)
                .withPriceAuxLimit(23)
                .withPriceTrailing(23)
                .withGroupId(4)
                .build();

        final BitfinexOrderCommand command = new BitfinexOrderCommand(order);

        final BitfinexApiBroker bitfinexApiBroker = buildMockedBitfinexConnection();

        final String commandValue = command.getCommand(bitfinexApiBroker);
        Assert.assertNotNull(commandValue);
        Assert.assertTrue(commandValue.length() > 10);
    }

    /**
     * Build the bitfinex connection
     *
     * @return
     */
    private BitfinexApiBroker buildMockedBitfinexConnection() {
        final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
        Mockito.when(bitfinexApiBroker.getApiKey()).thenReturn("abc");
        Mockito.when(bitfinexApiBroker.getApiSecret()).thenReturn("123");
        return bitfinexApiBroker;
    }
}

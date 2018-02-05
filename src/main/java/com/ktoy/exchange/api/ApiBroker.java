package com.ktoy.exchange.api;

import com.google.common.collect.Table;
import com.ktoy.exchange.api.commands.AbstractAPICommand;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.ConnectionCapabilities;
import com.ktoy.exchange.api.entity.Wallet;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import com.ktoy.exchange.api.manager.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public interface ApiBroker {
    WebsocketClientEndpoint getWebsocketEndpoint();

    AtomicLong getLastHeatbeat();

    void sendCommand(final AbstractAPICommand apiCommand);

    void connect() throws APIException;

    boolean reconnect();

    QuoteManager getQuoteManager();

    OrderbookManager getOrderbookManager();

    RawOrderbookManager getRawOrderbookManager();

    void updateConnectionHeartbeat();

    void addToChannelSymbolMap(final int channelId, final ChannelSymbol symbol);

    ChannelSymbol getFromChannelSymbolMap(int channelId);

    void removeChannel(int channelId);

    CountDownLatch getConnectionReadyLatch();

    void setAuthenticated(boolean b);

    void setCapabilities(ConnectionCapabilities capabilities);

    OrderManager getOrderManager();

    String getApiKey();

    PositionManager getPositionManager();

    TradeManager getTradeManager();

    Table<String,String,Wallet> getWalletTable();

    String getApiSecret();

    ExecutorService getExecutorService();

    int getChannelForSymbol(ChannelSymbol currencyPair);

    boolean removeChannelForSymbol(ChannelSymbol currencyPair);

    void close();

    ConnectionCapabilities getCapabilities();

    boolean isTickerActive(ChannelSymbol symbol);
}

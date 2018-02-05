package com.ktoy.exchange.api.entity;

public interface ExchangeOrder {
    long getOrderId();

    ExchangeOrderState getState();

    String getApikey();

    long getCid();

    String getSymbol();

    void setSymbol(String product_id);

    void setPrice(final double price);

    void setAmount(double amount);

    void setOrderType(OrderType orderType);
}

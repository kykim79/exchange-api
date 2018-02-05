package com.cf.data.map.poloniex;

import com.cf.data.model.poloniex.*;
import com.cf.data.model.poloniex.deserialize.PoloniexChartDataDeserializer;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author David
 */
public class PoloniexDataMapper {

    private final static Logger LOGGER = LoggerFactory.getLogger(PoloniexDataMapper.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
    private final static String EMPTY_RESULTS = "[]";
    private final static String INVALID_CHART_DATA_DATE_RANGE_RESULT = "[{\"date\":0,\"high\":0,\"low\":0,\"open\":0,\"close\":0,\"volume\":0,\"quoteVolume\":0,\"weightedAverage\":0}]";
    private final static String INVALID_CHART_DATA_CURRENCY_PAIR_RESULT = "{\"error\":\"Invalid currency pair.\"}";
    private final Gson gson;

    public PoloniexDataMapper() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
                    @Override
                    public ZonedDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                        return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString(), DTF);
                    }
                }).registerTypeAdapter(PoloniexChartData.class, new PoloniexChartDataDeserializer())
                .create();
    }

    public List<PoloniexChartData> mapChartData(String chartDataResult) {

        if (INVALID_CHART_DATA_DATE_RANGE_RESULT.equals(chartDataResult) || INVALID_CHART_DATA_CURRENCY_PAIR_RESULT.equals(chartDataResult)) {
            return Collections.EMPTY_LIST;
        }

        List<PoloniexChartData> results;
        try {
            PoloniexChartData[] chartDataResults = gson.fromJson(chartDataResult, PoloniexChartData[].class);
            results = Arrays.asList(chartDataResults);
        } catch (JsonSyntaxException | DateTimeParseException ex) {
            LOGGER.error("Exception mapping chart data {} - {}", chartDataResult, ex.getMessage());
            results = Collections.EMPTY_LIST;
        }
        return results;

    }

    public PoloniexFeeInfo mapFeeInfo(String feeInfoResult) {
        PoloniexFeeInfo feeInfo = gson.fromJson(feeInfoResult, new TypeToken<PoloniexFeeInfo>() {
        }.getType());

        return feeInfo;
    }

    public PoloniexActiveLoanTypes mapActiveLoans(String activeLoansResult) {
        PoloniexActiveLoanTypes activeLoanTypes = gson.fromJson(activeLoansResult, PoloniexActiveLoanTypes.class);

        return activeLoanTypes;
    }

    public Map<String, PoloniexTicker> mapTicker(String tickerData) {
        return gson.fromJson(tickerData, new TypeToken<Map<String, PoloniexTicker>>() {
        }.getType());
    }

    public PoloniexTicker mapTickerForCurrency(String currencyType, String tickerData) {
        return mapTicker(tickerData).get(currencyType);
    }

    public List<String> mapMarkets(String tickerData) {
        return new ArrayList<>(mapTicker(tickerData).keySet());
    }

    public Map<String, PoloniexCompleteBalance> mapCompleteBalanceResult(String completeBalanceResults) {
        return gson.fromJson(completeBalanceResults, new TypeToken<Map<String, PoloniexCompleteBalance>>() {
        }.getType());
    }

    public Map<String, PoloniexCompleteBalance> mapCompleteBalanceResultForNonZeroCurrencies(String completeBalanceResults) {
        return mapCompleteBalanceResult(completeBalanceResults).entrySet()
                .stream()
                .filter(balance -> balance.getValue().btcValue.compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toMap(balance -> balance.getKey(), balance -> balance.getValue()));
    }

    public PoloniexCompleteBalance mapCompleteBalanceResultForCurrency(String currencyType, String completeBalanceResults) {
        return mapCompleteBalanceResult(completeBalanceResults).get(currencyType);
    }

    public List<PoloniexOpenOrder> mapOpenOrders(String openOrdersResults) {
        List<PoloniexOpenOrder> openOrders = gson.fromJson(openOrdersResults, new TypeToken<List<PoloniexOpenOrder>>() {
        }.getType());
        return openOrders;
    }

    public List<PoloniexTradeHistory> mapTradeHistory(String tradeHistoryResults) {
        List<PoloniexTradeHistory> tradeHistory = gson.fromJson(tradeHistoryResults, new TypeToken<List<PoloniexTradeHistory>>() {
        }.getType());
        return tradeHistory;
    }

    public boolean mapCancelOrder(String cancelOrderResult) {
        int success = gson.fromJson(cancelOrderResult, JsonObject.class).get("success").getAsInt();
        return success == 1;
    }

    public PoloniexOrderResult mapTradeOrder(String orderResult) {
        PoloniexOrderResult tradeOrderResult = gson.fromJson(orderResult, new TypeToken<PoloniexOrderResult>() {
        }.getType());
        return tradeOrderResult;
    }

    public List<PoloniexLendingHistory> mapLendingHistory(String lendingHistoryResults) {
        List<PoloniexLendingHistory> lendingHistory = gson.fromJson(lendingHistoryResults, new TypeToken<List<PoloniexLendingHistory>>() {
        }.getType());
        return lendingHistory;
    }

    public List<PoloniexLoanOffer> mapOpenLoanOffers(String currency, String results) {
        if (EMPTY_RESULTS.equals(results)) {
            return Collections.EMPTY_LIST;
        }
        Map<String, List<PoloniexLoanOffer>> offers = gson.fromJson(results, new TypeToken<Map<String, List<PoloniexLoanOffer>>>() {
        }.getType());
        return offers.get(currency);
    }

    public PoloniexLendingResult mapLendingResult(String result) {
        PoloniexLendingResult plr = gson.fromJson(result, new TypeToken<PoloniexLendingResult>() {
        }.getType());
        return plr;
    }

}

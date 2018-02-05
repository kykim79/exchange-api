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

import com.ktoy.exchange.api.entity.RawOrderbookConfiguration;
import com.ktoy.exchange.api.entity.symbol.BitfinexCurrencyPair;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;

public class RawOrderbookTest {

    /**
     * Test the equals method
     */
    @Test
    public void testTradingOrderbookEquals() {
        final RawOrderbookConfiguration configuration1 = new RawOrderbookConfiguration(
                BitfinexCurrencyPair.BCH_USD);

        final RawOrderbookConfiguration configuration2 = new RawOrderbookConfiguration(
                BitfinexCurrencyPair.BCH_USD);

        final RawOrderbookConfiguration configuration3 = new RawOrderbookConfiguration(
                BitfinexCurrencyPair.AVT_BTC);

        Assert.assertEquals(configuration1.hashCode(), configuration2.hashCode());
        Assert.assertEquals(configuration1, configuration2);
        Assert.assertFalse(configuration1.equals(configuration3));
    }

    /**
     * Test the build from JSON array
     */
    @Test
    public void createRawOrderbookConfigurationFromJSON() {
        final String message = "{\"event\":\"subscribed\",\"channel\":\"book\",\"chanId\":3829,\"symbol\":\"tBTCUSD\",\"prec\":\"R0\",\"pair\":\"BTCUSD\"}";
        final JSONTokener tokener = new JSONTokener(message);
        final JSONObject jsonObject = new JSONObject(tokener);

        final RawOrderbookConfiguration configuration
                = RawOrderbookConfiguration.fromJSON(jsonObject);

        Assert.assertEquals(BitfinexCurrencyPair.BTC_USD, configuration.getCurrencyPair());
    }
}

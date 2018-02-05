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
package com.ktoy.exchange.api.commands;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.ktoy.exchange.api.ApiBroker;
import org.json.JSONObject;

import com.google.common.io.BaseEncoding;

public class BitfinexAuthCommand extends AbstractAPICommand {

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA384";

	@Override
	public String getCommand(final ApiBroker apiBroker) throws CommandException {
		try {
			final String APIKey = apiBroker.getApiKey();
			final String APISecret = apiBroker.getApiSecret();
			
			final String authNonce = Long.toString(System.currentTimeMillis());
			final String authPayload = "AUTH" + authNonce;

			final SecretKeySpec signingKey = new SecretKeySpec(APISecret.getBytes(), HMAC_SHA1_ALGORITHM);
			final Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			
			final byte[] encodedBytes = mac.doFinal(authPayload.getBytes());		
			final String authSig = BaseEncoding.base16().encode(encodedBytes);
			
			final JSONObject subscribeJson = new JSONObject();
			subscribeJson.put("event", "auth");
			subscribeJson.put("apiKey", APIKey);
			subscribeJson.put("authSig", authSig.toLowerCase());
			subscribeJson.put("authPayload", authPayload);
			subscribeJson.put("authNonce", authNonce);
			
			return subscribeJson.toString();
		} catch (Exception e) {
			throw new CommandException(e);
		} 
	}

}

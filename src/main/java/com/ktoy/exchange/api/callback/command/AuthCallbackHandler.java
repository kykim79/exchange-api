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
package com.ktoy.exchange.api.callback.command;

import com.ktoy.exchange.api.ApiBroker;
import com.ktoy.exchange.api.entity.APIException;
import com.ktoy.exchange.api.entity.ConnectionCapabilities;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class AuthCallbackHandler implements CommandCallbackHandler {

	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(AuthCallbackHandler.class);
	
	@Override
	public void handleChannelData(final ApiBroker apiBroker, final JSONObject jsonObject)
			throws APIException {

		final String status = jsonObject.getString("status");
		
		final CountDownLatch connectionReadyLatch = apiBroker.getConnectionReadyLatch();
		
		logger.info("Authentification callback state {}", status);
		
		if(status.equals("OK")) {
			apiBroker.setAuthenticated(true);
			final ConnectionCapabilities capabilities = new ConnectionCapabilities(jsonObject);
			apiBroker.setCapabilities(capabilities);
			
			if(connectionReadyLatch != null) {
				connectionReadyLatch.countDown();
			}
			
		} else {
			apiBroker.setAuthenticated(false);
			logger.error("Unable to authenticate: {}", jsonObject.toString());
			
			if(connectionReadyLatch != null) {
				// No other callbacks are send from the server after a failed auth call
				while(connectionReadyLatch.getCount() != 0) {
					connectionReadyLatch.countDown();
				}
			}
		}
	}
}

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

import java.util.concurrent.CountDownLatch;

import com.ktoy.exchange.api.GdaxApiBroker;
import com.ktoy.exchange.api.commands.BitfinexPingCommand;
import com.ktoy.exchange.api.commands.GdaxPingCommand;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ktoy.exchange.api.BitfinexApiBroker;
import com.ktoy.exchange.api.HeartbeatThread;
import com.ktoy.exchange.api.WebsocketClientEndpoint;
import com.ktoy.exchange.api.callback.api.HeartbeatHandler;
import com.ktoy.exchange.api.entity.APIException;


public class HeartbeatManagerTest {

	@Test(timeout=30000)
	public void testConnectWhenDisconnected() throws Exception {
		
		// Events
		// * Disconnect websocket from heartbeat
		// * Disconnect from bitfinex API reconnect method
		// * Reconnect on bitfinex api
		final CountDownLatch connectLatch = new CountDownLatch(3);
		
		// Count down the latch on method call
		final Answer<Void> answer = new Answer<Void>() {
	        public Void answer(final InvocationOnMock invocation) throws Throwable {
	        		connectLatch.countDown();
	        		return null;
	        }
		};
		
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);

		final BitfinexPingCommand pingCommand = Mockito.mock(BitfinexPingCommand.class);

		final HeartbeatThread heartbeatThreadRunnable = new HeartbeatThread(bitfinexApiBroker, pingCommand );
		final WebsocketClientEndpoint websocketClientEndpoint = Mockito.mock(WebsocketClientEndpoint.class);
		Mockito.when(websocketClientEndpoint.isConnected()).thenReturn(connectLatch.getCount() == 0);
		Mockito.when(bitfinexApiBroker.getWebsocketEndpoint()).thenReturn(websocketClientEndpoint);

		Mockito.doAnswer(answer).when(bitfinexApiBroker).reconnect();
		Mockito.doAnswer(answer).when(websocketClientEndpoint).close();
		
		final Thread heartbeatThread = new Thread(heartbeatThreadRunnable);
		
		try {
			heartbeatThread.start();
			connectLatch.await();
		} catch (Exception e) {
			// Should not happen
			throw e;
		} finally {
			heartbeatThread.interrupt();
		}
	}

	@Test(timeout=30000)
	public void testConnectWhenDisconnectedGdax() throws Exception {

		// Events
		// * Disconnect websocket from heartbeat
		// * Disconnect from bitfinex API reconnect method
		// * Reconnect on bitfinex api
		final CountDownLatch connectLatch = new CountDownLatch(3);

		// Count down the latch on method call
		final Answer<Void> answer = new Answer<Void>() {
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				connectLatch.countDown();
				return null;
			}
		};

		final GdaxApiBroker gdaxApiBroker = Mockito.mock(GdaxApiBroker.class);
		final GdaxPingCommand pingCommand = Mockito.mock(GdaxPingCommand.class);

		final HeartbeatThread heartbeatThreadRunnable = new HeartbeatThread(gdaxApiBroker, pingCommand );
		final WebsocketClientEndpoint websocketClientEndpoint = Mockito.mock(WebsocketClientEndpoint.class);
		Mockito.when(websocketClientEndpoint.isConnected()).thenReturn(connectLatch.getCount() == 0);
		Mockito.when(gdaxApiBroker.getWebsocketEndpoint()).thenReturn(websocketClientEndpoint);

		Mockito.doAnswer(answer).when(gdaxApiBroker).reconnect();
		Mockito.doAnswer(answer).when(websocketClientEndpoint).close();

		final Thread heartbeatThread = new Thread(heartbeatThreadRunnable);

		try {
			heartbeatThread.start();
			connectLatch.await();
		} catch (Exception e) {
			// Should not happen
			throw e;
		} finally {
			heartbeatThread.interrupt();
		}
	}
	
	/**
	 * Test the heartbeart handler
	 * @throws APIException 
	 */
	@Test
	public void testHeartbeatHandler() throws APIException {
		final BitfinexApiBroker bitfinexApiBroker = Mockito.mock(BitfinexApiBroker.class);
		final HeartbeatHandler handler = new HeartbeatHandler();
		handler.handleChannelData(bitfinexApiBroker, null);
		Mockito.verify(bitfinexApiBroker, Mockito.times(1)).updateConnectionHeartbeat();
	}
}

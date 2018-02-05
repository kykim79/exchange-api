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
package com.ktoy.exchange.api;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

@ClientEndpoint
public class WebsocketClientEndpoint implements Closeable {

	/**
	 * The user session
	 */
	private Session userSession = null;

	/**
	 * The callback consumer
	 */
	private final List<Consumer<String>> callbackConsumer;
	
	/**
	 * The wait for connection latch
	 */
	private final CountDownLatch connectLatch = new CountDownLatch(1);

	/**
	 * The endpoint URL
	 */
	private final URI endpointURI;
	
	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(WebsocketClientEndpoint.class);

	public WebsocketClientEndpoint(final URI endpointURI) {
		this.endpointURI = endpointURI;
		this.callbackConsumer = new ArrayList<>();
	}

	/**
	 * Open a new connection and wait until connection is ready
	 * 
	 * @throws DeploymentException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void connect() throws DeploymentException, IOException, InterruptedException {
		final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		this.userSession = container.connectToServer(this, endpointURI);
		connectLatch.await();
	}

	@OnOpen
	public void onOpen(final Session userSession) {
		logger.info("Websocket is now open");
		connectLatch.countDown();
	}

	@OnClose
	public void onClose(final Session userSession, final CloseReason reason) {
		logger.info("Closing websocket: " + reason);
		this.userSession = null;
	}

	@OnMessage(maxMessageSize=1048576)
	public void onMessage(final String message) {
		
		// Execute callbacks in another thread
		synchronized (callbackConsumer) {
			callbackConsumer.forEach((c) -> c.accept(message));
		}
	}
	
	@OnError
    public void onError(final Session session, final Throwable t) {
        logger.error("OnError called {}", Throwables.getStackTraceAsString(t));
    }

	/**
	 * Send a new message to the server
	 * @param message
	 */
	public void sendMessage(final String message) {
		
		if(userSession == null) {
			logger.error("Unable to send message, user session is null");
			return;
		}
		
		if(userSession.getAsyncRemote() == null) {
			logger.error("Unable to send message, async remote is null");
			return;
		}
		
		userSession.getAsyncRemote().sendText(message);
	}

	/**
	 * Add a new connection data consumer
	 * @param consumer
	 */
	public void addConsumer(final Consumer<String> consumer) {
		synchronized (callbackConsumer) {
			callbackConsumer.add(consumer);
		}
	}

	/**
	 * Remove a connection data consumer
	 * @param consumer
	 * @return
	 */
	public boolean removeConsumer(final Consumer<String> consumer) {
		synchronized (callbackConsumer) {
			return callbackConsumer.remove(consumer);
		}
	}

	/**
	 * Close the connection
	 */
	@Override
	public void close() {
		if(userSession == null) {
			return;
		}
		
		try {
			userSession.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Socket closed"));
		} catch (Throwable e) {
			logger.error("Got exception while closing socket", e);
		}
		
		userSession = null;
	}
	
	/**
	 * Is this websocket connected
	 * @return
	 */
	public boolean isConnected() {
		return userSession != null;
	}
}
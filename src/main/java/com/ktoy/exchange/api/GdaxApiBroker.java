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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.ktoy.exchange.api.callback.api.*;
import com.ktoy.exchange.api.callback.channel.GdaxTickHandler;
import com.ktoy.exchange.api.callback.command.CommandCallbackHandler;
import com.ktoy.exchange.api.callback.command.ErrorCommandCallback;
import com.ktoy.exchange.api.callback.command.GdaxSubscribedCallback;
import com.ktoy.exchange.api.callback.command.UnsubscribedCallback;
import com.ktoy.exchange.api.commands.*;
import com.ktoy.exchange.api.entity.*;
import com.ktoy.exchange.api.entity.symbol.ChannelSymbol;
import com.ktoy.exchange.api.entity.symbol.GdaxCandlestickSymbol;
import com.ktoy.exchange.api.entity.symbol.GdaxCurrencyPair;
import com.ktoy.exchange.api.manager.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class GdaxApiBroker implements Closeable, ApiBroker {

	/**
	 * The Websocket URI
	 */
	public final static String WSS_URI = "wss://ws-feed.gdax.com";

	/**
	 * The API callback
	 */
	private final Consumer<String> apiCallback = ((c) -> websocketCallback(c));

	/**
	 * The websocket endpoint
	 */
	private WebsocketClientEndpoint websocketEndpoint;

	/**
	 * The channel map
	 */
	private final Map<Integer, ChannelSymbol> channelIdSymbolMap;

	/**
	 * The tick manager
	 */
	private final QuoteManager quoteManager;

	/**
	 * The trading orderbook manager
	 */
	private final OrderbookManager orderbookManager;

	/**
	 * The trading RAW orderbook manager
	 */
	private final RawOrderbookManager rawOrderbookManager;

	/**
	 * The position manager
	 */
	private final PositionManager positionManager;

	/**
	 * The order manager
	 */
	private final OrderManager orderManager;

	/**
	 * The trade manager
	 */
	private final TradeManager tradeManager;

	/**
	 * The last heartbeat value
	 */
	protected final AtomicLong lastHeatbeat;

	/**
	 * The heartbeat thread
	 */
	private Thread heartbeatThread;

	/**
	 * The API key
	 */
	private String apiKey;

	/**
	 * The API secret
	 */
	private String apiSecret;

	/**
	 * The connection ready latch
	 */
	private CountDownLatch connectionReadyLatch;

	/**
	 * Event on the latch until the connection is ready
	 * - Authenticated
	 * - Order snapshots read
	 * - Wallet snapshot read
	 * - Position snapshot read
	 */
	private final static int CONNECTION_READY_EVENTS = 4;

	/**
	 * The capabilities of the connection
	 */
	private ConnectionCapabilities capabilities = ConnectionCapabilities.NO_CAPABILITIES;

	/**
	 * Is the connection authenticated?
	 */
	private boolean authenticated;

	/**
	 * Wallets
	 *
	 *  Currency, Wallet-Type, Wallet
	 */
	private final Table<String, String, Wallet> walletTable;

	/**
	 * The channel handler
	 */
	private final Map<String, APICallbackHandler> channelHandler;

	/**
	 * The command callbacks
	 */
	private Map<String, CommandCallbackHandler> commandCallbacks;

	/**
	 * The executor service
	 */
	private final ExecutorService executorService;


	private Set<String> commandSet;

	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(GdaxApiBroker.class);


	public GdaxApiBroker(final String apiKey, final String apiSecret) {
		this();
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}

	public GdaxApiBroker() {
		this.executorService = Executors.newFixedThreadPool(10);
		this.channelIdSymbolMap = new HashMap<>();
		this.lastHeatbeat = new AtomicLong();
		this.quoteManager = new QuoteManager(this);
		this.orderbookManager = new OrderbookManager(this);
		this.rawOrderbookManager = new RawOrderbookManager(this);
		this.orderManager = new OrderManager(this);
		this.tradeManager = new TradeManager(this);
		this.positionManager = new PositionManager(executorService);
		this.walletTable = HashBasedTable.create();
		this.capabilities = ConnectionCapabilities.NO_CAPABILITIES;
		this.authenticated = false;
		this.channelHandler = new HashMap<>();

		setupChannelHandler();
		setupCommandCallbacks();

		commandSet = new HashSet<>();
	}

	/**
	 * Setup the channel handler
	 */
	private void setupChannelHandler() {
		// Heartbeat
		channelHandler.put("heartbeat", new HeartbeatHandler());

		// order book
		channelHandler.put("l2update", new GdaxOrderHandler());

		// trade
		channelHandler.put("match", new GdaxTradeHandler());
	}
	
	/**
	 * Setup the command callbacks
	 */
	private void setupCommandCallbacks() {
		commandCallbacks = new HashMap<>();
		commandCallbacks.put("subscriptions", new GdaxSubscribedCallback());
		commandCallbacks.put("ticker", new GdaxSubscribedCallback());
		commandCallbacks.put("snapshot", new GdaxSubscribedCallback());
		commandCallbacks.put("l2update", new GdaxSubscribedCallback());
		commandCallbacks.put("heartbeat", new GdaxSubscribedCallback());
		commandCallbacks.put("unsubscribe", new UnsubscribedCallback());
		commandCallbacks.put("error", new ErrorCommandCallback());
		commandCallbacks.put("received", new GdaxSubscribedCallback());
		commandCallbacks.put("open", new GdaxSubscribedCallback());
		commandCallbacks.put("done", new GdaxSubscribedCallback());
		commandCallbacks.put("match", new GdaxSubscribedCallback());
		commandCallbacks.put("change", new GdaxSubscribedCallback());
		commandCallbacks.put("activate", new GdaxSubscribedCallback());
	}
	
	/**
	 * Open the connection
	 * @throws APIException
	 */
	public void connect() throws APIException {
		try {
			final URI bitfinexURI = new URI(WSS_URI);
			websocketEndpoint = new WebsocketClientEndpoint(bitfinexURI);
			websocketEndpoint.addConsumer(apiCallback);
			websocketEndpoint.connect();
			updateConnectionHeartbeat();
			
			executeAuthentification();
			
			heartbeatThread = new Thread(new HeartbeatThread(this, new GdaxPingCommand(GdaxCurrencyPair.BTC_USD)));
			heartbeatThread.start();
		} catch (Exception e) {
			throw new APIException(e);
		}
	}

	/**
	 * Execute the authentication and wait until the socket is ready
	 * @throws InterruptedException
	 * @throws APIException 
	 */
	private void executeAuthentification() throws InterruptedException, APIException {
		connectionReadyLatch = new CountDownLatch(CONNECTION_READY_EVENTS);

		if(isAuthenticatedConnection()) {
			sendCommand(new BitfinexAuthCommand());
			logger.info("Waiting for connection ready events");
			connectionReadyLatch.await(10, TimeUnit.SECONDS);
			
			if(! authenticated) {
				throw new APIException("Unable to perform authentification, capabilities are: " + capabilities);
			}
		}
	}

	/**
	 * Is the connection to be authenticated
	 * @return
	 */
	private boolean isAuthenticatedConnection() {
		return apiKey != null && apiSecret != null;
	}

	/**
	 * Disconnect the websocket
	 */
	@Override
	public void close() {
		
		if(heartbeatThread != null) {
			heartbeatThread.interrupt();
			heartbeatThread = null;
		}
		
		if(websocketEndpoint != null) {
			websocketEndpoint.removeConsumer(apiCallback);
			websocketEndpoint.close();
			websocketEndpoint = null;
		}
		
		if(executorService != null) {
			executorService.shutdown();
		}
	}

	/**
	 * Send a new API command
	 * @param apiCommand
	 */
	public void sendCommand(final AbstractAPICommand apiCommand) {
		try {
			final String command = apiCommand.getCommand(this);
			logger.debug("Sending to server: {}", command);
			websocketEndpoint.sendMessage(command);
		} catch (CommandException e) {
			logger.error("Got Exception while sending command", e);
		}
	}
	
	/**
	 * Get the websocket endpoint
	 * @return
	 */
	public WebsocketClientEndpoint getWebsocketEndpoint() {
		return websocketEndpoint;
	}
	
	/**
	 * We received a websocket callback
	 * @param message
	 */
	private void websocketCallback(final String message) {
		//logger.debug("Got message: {}", message);

		final JSONObject jsonObject = new JSONObject(new JSONTokener(message));
		String type = jsonObject.getString("type");
		if(commandCallbacks.containsKey(type)) {
			if(!commandSet.contains(type)) {
				commandSet.add(type);
				handleCommandCallback(jsonObject);
			} else {
				handleChannelCallback(jsonObject);
			}
		} else {
			logger.error("Got unknown callback: {}", message);
		}

	}

//	private GdaxCurrencyPair getPair(JSONObject jsonObject) {
//		GdaxCurrencyPair pair = null;
//		final String type = jsonObject.getString("type");
//		if(type.equalsIgnoreCase("subscriptions")) {
//			JSONArray jsonArray = jsonObject.getJSONArray("channels");
//			JSONObject o = jsonArray.getJSONObject(0);;
//			pair = GdaxCurrencyPair.fromSymbolString(o.getJSONArray("product_ids").getString(0));
//		} else {
//			pair = GdaxCurrencyPair.fromSymbolString(jsonObject.getString("product_id"));
//		}
//		return pair;
//	}

	/**
	 * Handle a command callback
	 * @param jsonObject
	 */
	private void handleCommandCallback(final JSONObject jsonObject) {
		
		//logger.debug("Got {}", message);
		
		final String eventType = jsonObject.getString("type");
		
		if(! commandCallbacks.containsKey(eventType)) {
			logger.error("Unknown event: {}", jsonObject);
		} else {
			try {
				final CommandCallbackHandler callback = commandCallbacks.get(eventType);
				callback.handleChannelData(this, jsonObject);
			} catch (APIException e) {
				logger.error("Got an exception while handling callback");
			}
		}
	}

	/**
	 * Remove the channel
	 * @param channelId
	 */
	public void removeChannel(final int channelId) {
		synchronized (channelIdSymbolMap) {
			channelIdSymbolMap.remove(channelId);
			channelIdSymbolMap.notifyAll();
		}
	}

	/**
	 * Update the connection heartbeat
	 */
	public void updateConnectionHeartbeat() {
		lastHeatbeat.set(System.currentTimeMillis());
	}

	/**
	 * Add channel to symbol map
	 * @param channelId
	 * @param symbol
	 */
	public void addToChannelSymbolMap(final int channelId, final ChannelSymbol symbol) {
		synchronized (channelIdSymbolMap) {
			channelIdSymbolMap.put(channelId, symbol);
			channelIdSymbolMap.notifyAll();
		}
	}

	/**
	 * Handle a channel callback
	 * @param jsonObject
	 */
	protected void handleChannelCallback(final JSONObject jsonObject) {
		// Channel callback
		logger.debug("Channel callback");
		updateConnectionHeartbeat();

//		final int channel = 0;

		String type = jsonObject.getString("type");
		if(!type.equalsIgnoreCase("ticker")) {
			handleSignalingChannelData(jsonObject);
		} else {
			handleChannelData(jsonObject);
		}
	}

	/**
	 * Handle signaling channel data
	 * @param jsonObject
	 */
	private void handleSignalingChannelData(final JSONObject jsonObject) {

		String type = jsonObject.getString("type");
		if(type.equalsIgnoreCase("error")) {
			logger.error("Got Error message: {}", jsonObject.getString("message"));
		} else if (!type.equalsIgnoreCase("subscriptions")){

			if(!channelHandler.containsKey(type)) {
				logger.debug("Unsupported channel: {}",type);
				return;
			}
			final APICallbackHandler channelHandlerCallback = channelHandler.get(type);

			try {
				JSONArray jsonArray = new JSONArray();
				jsonArray.put(jsonObject);
				channelHandlerCallback.handleChannelData(this, jsonArray);
			} catch (APIException e) {
				logger.error("Got exception while handling callback", e);
			} catch (Exception e) {
				logger.error("Got exception while handling callback", e);
			}
		}
	}

	private void handleChannelData(final JSONObject jsonObject) {
		ChannelSymbol channelSymbol = null;

		try {
			String type = jsonObject.getString("type");
			switch(type) {
				case "ticker": {
					channelSymbol = GdaxCurrencyPair.fromSymbolString(jsonObject.getString("product_id"));
					GdaxTickHandler handler = new GdaxTickHandler();
					JSONArray jsonArray = new JSONArray();
					jsonArray.put(jsonObject);
					handler.handleChannelData(this, channelSymbol, jsonArray);
				}
			}
		} catch (APIException e) {
			logger.error("exception", e);
		}
	}

	/**
	 * Handle normal channel data
	 * @param jsonObject
	 * @throws APIException
	 */
//	private void handleChannelData(final JSONObject jsonObject) {
//		//final int channel = jsonArray.getInt(0);
//		//final ChannelSymbol channelSymbol = getFromChannelSymbolMap(0);
//
//		ChannelSymbol channelSymbol = getSymbol(jsonObject);
//		addToChannelSymbolMap(0, channelSymbol);
//
//		if(channelSymbol == null) {
//			logger.error("Unable to determine symbol for channel {}", 0);
//			logger.error("Data is {}", jsonObject);
//			return;
//		}
//
//			final JSONArray jsonArray = new JSONArray();
//			jsonArray.put(jsonObject);
////
//			try {
//				if(channelSymbol instanceof GdaxCandlestickSymbol) {
//					final ChannelCallbackHandler handler = new GdaxCandlestickHandler();
//					handler.handleChannelData(this, channelSymbol, jsonArray);
//				} else if(channelSymbol instanceof GdaxTickHandler) {
//					final GdaxTickHandler handler = new GdaxTickHandler();
//					handler.handleChannelData(this, channelSymbol, jsonArray);
////				} else if(channelSymbol instanceof OrderbookConfiguration) {
////					final OrderbookHandler handler = new OrderbookHandler();
////					handler.handleChannelData(this, channelSymbol, subarray);
////				} else if(channelSymbol instanceof ChannelSymbol) {
////					final ChannelCallbackHandler handler = new TickHandler();
////					handler.handleChannelData(this, channelSymbol, subarray);
////				}
// 				} else {
//					logger.error("Unknown stream type: {}", channelSymbol);
//				}
//			} catch (APIException e) {
//				logger.error("Got exception while handling callback", e);
//			}
////		}
//	}

	/**
	 * Get the channel from the symbol map - thread safe
	 * @param channel
	 * @return
	 */
	public ChannelSymbol getFromChannelSymbolMap(final int channel) {
		synchronized (channelIdSymbolMap) {
			return channelIdSymbolMap.get(channel);
		}
	}
	
	/**
	 * Test whether the ticker is active or not 
	 * @param symbol
	 * @return
	 */
	public boolean isTickerActive(final ChannelSymbol symbol) {
		return getChannelForSymbol(symbol) != -1;
	}

	/**
	 * Find the channel for the given symbol
	 * @param symbol
	 * @return
	 */
	public int getChannelForSymbol(final ChannelSymbol symbol) {
		synchronized (channelIdSymbolMap) {
			return channelIdSymbolMap.entrySet()
					.stream()
					.filter((v) -> v.getValue().equals(symbol))
					.map((v) -> v.getKey())
					.findAny().orElse(-1);
		}
	}
	
	/**
	 * Remove the channel for the given symbol
	 * @param symbol
	 * @return
	 */
	public boolean removeChannelForSymbol(final ChannelSymbol symbol) {
		final int channel = getChannelForSymbol(symbol);
		
		if(channel != -1) {
			synchronized (channelIdSymbolMap) {
				channelIdSymbolMap.remove(channel);
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Perform a reconnect
	 * @return
	 */
	public synchronized boolean reconnect() {
		try {
			logger.info("Performing reconnect");
			capabilities = ConnectionCapabilities.NO_CAPABILITIES;
			authenticated = false;
			
			// Invalidate old data
			quoteManager.invalidateTickerHeartbeat();
			orderManager.clear();
			positionManager.clear();
			
			websocketEndpoint.close();
			websocketEndpoint.connect();
			
			executeAuthentification();
			resubscribeChannels();

			updateConnectionHeartbeat();
			
			return true;
		} catch (Exception e) {
			logger.error("Got exception while reconnect", e);
			websocketEndpoint.close();
			return false;
		}
	}

	/**
	 * Resubscribe the old ticker
	 * @throws InterruptedException
	 * @throws APIException
	 */
	private void resubscribeChannels() throws InterruptedException, APIException {
		final Map<Integer, ChannelSymbol> oldChannelIdSymbolMap = new HashMap<>();

		synchronized (channelIdSymbolMap) {
			oldChannelIdSymbolMap.putAll(channelIdSymbolMap);
			channelIdSymbolMap.clear();
			channelIdSymbolMap.notifyAll();
		}
		
		// Resubscribe channels
		for(ChannelSymbol symbol : oldChannelIdSymbolMap.values()) {
			if(symbol instanceof GdaxCurrencyPair) {
				sendCommand(new GdaxSubscribeTickerCommand((GdaxCurrencyPair) symbol));
			} else if(symbol instanceof GdaxCandlestickSymbol) {
				sendCommand(new BitfinexSubscribeCandlesCommand((GdaxCandlestickSymbol) symbol));
			} else if(symbol instanceof OrderbookConfiguration) {
				sendCommand(new BitfinexSubscribeOrderbookCommand((OrderbookConfiguration) symbol));
			} else if(symbol instanceof RawOrderbookConfiguration) {
				sendCommand(new BitfinexSubscribeRawOrderbookCommand((RawOrderbookConfiguration) symbol));
			} else {
				logger.error("Unknown stream symbol: {}", symbol);
			}
		}
		
		logger.info("Waiting for streams to resubscribe");
		int execution = 0;
		
		synchronized (channelIdSymbolMap) {		
			while(channelIdSymbolMap.size() != oldChannelIdSymbolMap.size()) {
				
				if(execution > 10) {
					
					// Restore old map for reconnect
					synchronized (channelIdSymbolMap) {
						channelIdSymbolMap.clear();
						channelIdSymbolMap.putAll(oldChannelIdSymbolMap);
					}
					
					throw new APIException("Subscription of ticker failed");
				}
				
				channelIdSymbolMap.wait(500);
				execution++;	
			}
		}
	}
	
	/**
	 * Get the last heartbeat value
	 * @return
	 */
	public AtomicLong getLastHeatbeat() {
		return lastHeatbeat;
	}
	
	/**
	 * Get the API key
	 * @return
	 */
	public String getApiKey() {
		return apiKey;
	}
	
	/**
	 * Get the API secret
	 * @return
	 */
	public String getApiSecret() {
		return apiSecret;
	}
	
	/**
	 * Get all wallets
	 * @return 
	 * @throws APIException 
	 */
	public Collection<Wallet> getWallets() throws APIException {		
		
		throwExceptionIfUnauthenticated();
		
		synchronized (walletTable) {
			return Collections.unmodifiableCollection(walletTable.values());
		}
	}
	
	/**
	 * Get all wallets
	 * @return 
	 * @throws APIException 
	 */
	public Table<String, String, Wallet> getWalletTable() {
		return walletTable;
	}

	/**
	 * Throw a new exception if called on a unauthenticated connection
	 * @throws APIException
	 */
	private void throwExceptionIfUnauthenticated() throws APIException {
		if(! authenticated) {
			throw new APIException("Unable to perform operation on an unauthenticated connection");
		}
	}

	/**
	 * Get the ticker manager
	 * @return
	 */
	public QuoteManager getQuoteManager() {
		return quoteManager;
	}
	
	/**
	 * Get the connection ready latch
	 * @return
	 */
	public CountDownLatch getConnectionReadyLatch() {
		return connectionReadyLatch;
	}

	/**
	 * Get the executor service
	 * @return
	 */
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
	/**
	 * Get the order manager
	 * @return
	 */
	public OrderManager getOrderManager() {
		return orderManager;
	}
	
	/**
	 * Get the trade manager
	 * @return
	 */
	public TradeManager getTradeManager() {
		return tradeManager;
	}
	
	/**
	 * Get the orderbook manager
	 * @return
	 */
	public OrderbookManager getOrderbookManager() {
		return orderbookManager;
	}

	/**
	 * Get the raw orderbook manager
	 * @return
	 */
	public RawOrderbookManager getRawOrderbookManager() {
		return rawOrderbookManager;
	}
	
	/**
	 * Get the position manager
	 * @return
	 */
	public PositionManager getPositionManager() {
		return positionManager;
	}
	
	/**
	 * Set new connection capabilities
	 * @param capabilities
	 */
	public void setCapabilities(final ConnectionCapabilities capabilities) {
		this.capabilities = capabilities;
	}
	
	/**
	 * Get the connection capabilities
	 * @return
	 */
	public ConnectionCapabilities getCapabilities() {
		return capabilities;
	}
	
	/**
	 * Is the connection authenticated
	 * @return
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}
	
	/**
	 * Set connection auth status
	 * @param authenticated
	 */
	public void setAuthenticated(final boolean authenticated) {
		this.authenticated = authenticated;
	}
}

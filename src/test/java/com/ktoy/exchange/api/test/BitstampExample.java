package com.ktoy.exchange.api.test;

import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import org.json.JSONObject;

public class BitstampExample {
    public static void main(String[] args) throws Exception {
        final String BITSTAMP_PUSHER_KEY = "de504dc5763aeef9ff52";

        //PusherOptions options = new PusherOptions().setCluster(YOUR_APP_CLUSTER);
        Pusher pusher = new Pusher(BITSTAMP_PUSHER_KEY);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                System.out.println("State changed to " + change.getCurrentState() +
                        " from " + change.getPreviousState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                System.out.println("There was a problem connecting!");
            }
        }, ConnectionState.ALL);

        // Subscribe to a channel
        Channel channel = pusher.subscribe("live_trades");

        // Bind to listen for events called "my-event" sent to "my-channel"
        channel.bind("trade", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                JSONObject jsonObj = new JSONObject(data);
                //System.out.println(jsonObj);
                System.out.println(String.format("id: %s, time: %s, price: %s, amount: %s", jsonObj.get("id"), jsonObj.get("timestamp"), jsonObj.get("price"), jsonObj.get("amount")));
            }
        });

        // // Disconnect from the service (or become disconnected my network conditions)
        // pusher.disconnect();

        // Reconnect, with all channel subscriptions and event bindings automatically recreated
        pusher.connect();
        // The state change listener is notified when the connection has been re-established,
        // the subscription to "my-channel" and binding on "my-event" still exist.

        Thread.sleep(100000);
    }
}


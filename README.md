This project contains clients for multiple exchanges 
* the [Bitfinex WebSocket API (v2)](https://docs.bitfinex.com/v2/reference). 
* the [GDAX WebSocket API](https://docs.gdax.com/#websocket-feed).

At the moment, candles, ticks and (raw) orderbook streams are supported. In addition, orders, and wallets are also implemented.

In contrast to other implementations, this project uses the WSS streaming API of Exchange. Most other projects are poll the REST-API periodically, which leads to delays in data processing. 
In this implementation, you can register callback methods on ticks, candles or orders. The callbacks are executed, as soon as new data is received from Exchange (see the examples section for more details).

**Warning:** Trading carries significant financial risk; you could lose a lot of money. If you are planning to use this software to trade, you should perform many tests and simulations first. 
This software is provided 'as is' and released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0). 


## Contact / Stay informed
* You need help or do you have questions? Join our chat at [gitter](https://gitter.im/exchange-api/Lobby)
* For reporting issues, visit our [bug tracking system](https://github.com/kykim79/exchange-api/issues)
* For contributing, see our [contributing guide](https://github.com/kykim79/excahnge-api/blob/master/CONTRIBUTING.md)

# Adding the library to your project
Add this to your pom.xml 

```xml
<dependency>
	<groupId>com.github.kykim79</groupId>
	<artifactId>exchange-api</artifactId>
	<version>0.0.1</version>
</dependency>
```

# Changelog
You will find the changelog of the project [here](https://github.com/kykim79/exchange-api/blob/master/CHANGELOG.md).

# Examples
You will find some examples [here](https://github.com/kykim79/exchange-api/blob/master/EXAMPLES.md).


# A Reactive HTTP Client.

[![Build Status](https://travis-ci.org/WegenenVerkeer/RxHttpClient.png?branch=develop)](https://travis-ci.org/WegenenVerkeer/RxHttpClient)


This HTTP Client wraps the excellent [AsyncHttpClient](https://github.com/AsyncHttpClient/async-http-client) (AHC) so that
Observables are returned, and a number of best practices in RESTful integration are enforced.

# Upgrade to AHC 2

This version of RxHttpClient uses AHC 2.6. This implies a number of minor API changes w.r.t to the 0.x versions. 

API Changes:

 - The methods in ObservableBodyGenerators no longer declare that the throw `Exception`s
 - `ServerResponse#getResponseBody(String)` replaced by `ServerResponse#getResponseBody(String)`
 
The following methods have been removed:

 - `RxHttpClient.Builder#setExecutorService()`. Replaced by `RxHttpClient.Builder#setThreadFactory()`
 - `RxHttpClient.Builder#setHostnameVerifier()` 
 - `RxHttpClient.Builder#setUseRelativeURIsWithConnectProxies()`


The following methods have been deprecated:

 - `ClientRequest#getContentLength()`
 - `RxHttpClient.Builder#setAllowPoolingConnections(boolean)`: use `setKeepAlive()`
 - `RxHttpClient.Builder#setAcceptAnyCertificate(boolean)`: use `RxHttpClient.Builder#setUseInsecureTrustManager(boolean)`
 - `RxHttpClient.Builder setDisableUrlEncodingForBoundedRequests(boolean)`: use ` RxHttpClient.Builder#setDisableUrlEncodingForBoundRequests(boolean)`

# User Guide

## The RxHttpClient

The intent is that your application uses one `RxHttpClient` instance for each integration point (usually a REST service). Because creating
 an `RxHttpClient` is expensive, you should do this only once in your application. 
   
As `RxHttpClients` are limited to one service, we have natural bulkheads between integration points: errors and failures with 
respect to one integration point will have no direct effect on other integration points (at least if following the recommendations below).  


## Creating an RxHttpClient

An `RxHttpClient` is created using the `RxHttpClient.Builder` as in this example for Java:


    RxHttpClient client = new RxHttpClient.Builder()
                    .setRequestTimeout(REQUEST_TIME_OUT)
                    .setMaxConnections(MAX_CONNECTIONS)
                    .setConnectionTTL(60000)
                    .setConnectionTimeout(1000)
                    .setAccept("application/json")
                    .setBaseUrl("http://example.com/api")
                    .build();

and for Scala:

    import be.wegenenverkeer.rxhttp.scala.ImplicitConversions._

    val client = new RxHttpClient.Builder()
                        .setRequestTimeout(REQUEST_TIME_OUT)
                        .setMaxConnections(MAX_CONNECTIONS)
                        .setConnectionTTL(60000)
                        .setConnectionTimeout(1000)
                        .setAccept("application/json")
                        .setBaseUrl("http://example.com/api")
                        .build
                        .asScala


## Creating Requests

REST Requests can be created using `ClientRequestBuilders` which in turn can be got from `RxHttpClient` instances, like so:
 
    ClientRequest request = client.requestBuilder()
                    .setMethod("GET")
                    .setUrlRelativetoBase(path)
                    .addQueryParam("q", "test")
                    .build();

`ClientRequest`s are immutable so can be freely shared across threads or (Akka) Actors.

## Executing Requests

`RxHttpClient` has several methods for executing `ClientRequests`:
 
 + `executeToCompletion(ClientRequest, Function<ServerResponse, F>)` returns an `Observable<F>`. The second parameter is a function that decodes the 
 `ServerResponse` to a value of type `F`. The returned `Observable` emits exactly one `F` before completing. In case of an error, it 
    emits either an `HttpClientError` or `HttpServerError`. 
 + `execute(ClientRequest,  Function<ServerResponse, F>)` returns a `CompletableFuture<F>` with the response after being decoded by the function in the argument
 + `executeOservably(ClientRequest, Function<byte[], F>)` returns an `Observable<F>` which returns an `F` for each HTTP response part or chunk received. This
  is especially useful for processing HTTP responses that use chunked transfer encoding. Each chunk will be transformed to a value of `F` and 
  directly emitted by the Observable.

All Observables returned by these methods are "Cold" Observables. This means that the `ClientRequest` is executed only when some Observer subscribes 
to the Observable. In fact, whenever an Observer subscribes to the Observable, the request is executed.


## Recommended usage 

To allow proper bulkheading between integration points and the rest of your application, you should follow these recommendations:

+ Set the maximum number of Connections using method `RxHttpClient.Builder().setMaxConnections(int)` so that one misbehaving integration doesn't
 start exhausting server resources
+ Set an explicit Connection Time-To-Live (TTL) using method `RxHttpClient.builder().setConnectionTTL(int)`. This ensures that connections are 
regularly recreated which is a good thing in dynamic (clooud) environments
+ Ensure you have an appropriate Connection Timeout set using `RxHttpClient.Builder().setConnectionTimeout(int)`. The default is set to 5 seconds.
+ Ensure you have appropriate Request Timeouts and Read Timeouts set. The default for both is 1 min. These time outs ensures your application 
doesn't get stuck waiting for very slow or non-responsive servers.
+ Before discarding an `RxHttpClient` explicitly invoke the `RxHttpClient.close()` method its `ExecutorService` is closed and I/O threads are
destroyed





# A Reactive HTTP Client.

[![Build Status](https://travis-ci.org/WegenenVerkeer/RxHttpClient.png?branch=develop)](https://travis-ci.org/WegenenVerkeer/RxHttpClient)


This HTTP Client wraps the excellent [AsyncHttpClient](https://github.com/AsyncHttpClient/async-http-client) (AHC) so that
Observables are returned, and a number of best practices in RESTful integration are enforced.

# Version 2.x

## Overview of changes

- `RxHttpClient` is now an interface that exposes an API based on [Reactive Streams](https://github.com/reactive-streams/reactive-streams-jvm). This
 API is intended as a foundation for interoperability. It is not to be used in client code
- The primary implementation of `RxHttpClient` is `RxJavaHttpClient`, which is based on [RxJava 3](https://github.com/ReactiveX/RxJava)
- A java-interop libraries contains implementations for Spring `Reactor` and the jdk9 `Flow` API
- A scala `fs2` module, provides an alternative io.fs2.Streams-based API (see the [README](modules/fs2/README.md))  

## Design
This version is built primarily on:

 - [AsyncHttpClient 2.x](https://github.com/AsyncHttpClient/async-http-client)
 - [RxJava 3.x](https://github.com/ReactiveX/RxJava)
 
RxJava 3 is fully compatible with [Reactive Streams](https://github.com/reactive-streams/reactive-streams-jvm) which enables this library to 
work with with other Reactive-streams compatible libraries such as Reactor, Akka and FS2.

Although the JDK9 Flow API is semantically equivalent to the Reactive-Streams API, *it does not implement the 
Reactive Streams API*. For this reason, the `FlowHttpClient` is not an implementor of the `RxHttpClient` interface.

# User Guide

## The RxJavaHttpClient

The intent is that your application uses one `RxJavaHttpClient` instance for each integration point (usually a REST service). Because creating
 an `RxJavaHttpClient` is expensive, you should do this only once in your application. 
   
As `RxJavaHttpClients` are limited to one service, we have natural bulkheads between integration points: errors and failures with 
respect to one integration point will have no direct effect on other integration points (at least if following the recommendations below).  


### Creating an RxHttpClient

An `RxJavaHttpClient` is created using the `RxHttpClient.Builder` as in this example for Java:


    RxJavaHttpClient client = new RxJavaHttpClient.Builder()
                    .setRequestTimeout(REQUEST_TIME_OUT)
                    .setMaxConnections(MAX_CONNECTIONS)
                    .setConnectionTTL(60000)
                    .setConnectionTimeout(1000)
                    .setAccept("application/json")
                    .setBaseUrl("http://example.com/api")
                    .build();


### Creating Requests

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
 + `executeOservably(ClientRequest, Function<byte[], F>)` returns an `Observable<F>` that emits an `F` for each HTTP response part or chunk received. This
  is especially useful for processing HTTP responses that use chunked transfer encoding. Each chunk will be transformed to a value of `F` and 
  directly emitted by the Observable. Notice that there is no guarantee that the received chunks correspond exactly to the chunks as transmitted. 
  + `executeAndDechunk(ClientRequest, String)` returns an `Observable<String>` that emits a String whenever a separator String is observed 
  in the received chunks. This is especially useful when chunked transfer encoding is used for server sent events (SSE) or other streaming 
  data.  

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


# Notes when upgrading from versions prior to 1.0

Since version 1.0, RxHttpClient uses AHC 2.6.x. or later. This implies a number of minor API changes w.r.t to the 0.x versions. 

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



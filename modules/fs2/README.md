# Overview

This module provides interop with the https://github.com/functional-streams-for-scala/fs2[FS2] library.


# Getting Started

This module requires fs2, fs-reactive-streams on the classpath.

```
val fs2Version = ??? // any recent one should work
libraryDependencies ++= Seq(
    "co.fs2" %% "fs2-core" % fs2Version,
    "co.fs2" %% "fs2-reactive-streams" % fs2Version,
    "be.wegenenverkeer" %% "rxhttp-fs2" % "2.0-RC1")
```

This will pull the `be.wegenenverkeer.rxhttpClient` package in as a transitive dependency.

# API

This module provides a Streaming API `FSHttpApi`:

```
trait Fs2HttpApi {
  def stream[F[_] : ConcurrentEffect](request: ClientRequest): Stream[F, ServerResponseElement]
  def streamDechunked[F[_] : ConcurrentEffect](request: ClientRequest, separator: String, charset: Charset): Stream[F, String]
  def streamDechunked[F[_] : ConcurrentEffect](request: ClientRequest, separator: String): Stream[F, String]
  def stream[F[_] : ConcurrentEffect, A](req: ClientRequest, transform: Array[Byte] => A): Stream[F, A]
  def execute[F[_] : Async, A](req: ClientRequest, tr: ServerResponse => A): F[A]
  def requestBuilder: ClientRequestBuilder
  def requestSigners: util.List[RequestSigner]
}
```

After importing `fs2.Implicits._` a RxJavaHttpClient can be converted into an implementation
of this trait.


```
val client : RxJavaHttpClient = ???

val response = client.fs2HttpApi.stream[IO, String](request, b => new String(b))
val output = response.compile.toVector.unsafeRunSync()
```

# Usage

Here is an example that gets the response elements (chunks) in a `fs2.Stream[IO, String]`. Obviously,
the request will only fire when the effect is run.

```
import scala.concurrent.ExecutionContext
implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
import be.wegenenverkeer.rxhttp.fs2.Implicits._

val client : RxJavaHttpClient = ???
val response = client.fs2HttpApi.stream[IO, String](request, b => new String(b))
```

We can also return the complete response as a single value wrapped in an `IO`.

```
val resp = client.fs2HttpApi.execute[IO, String](request, sr => sr.getResponseBody)
//resp : IO[String]
```




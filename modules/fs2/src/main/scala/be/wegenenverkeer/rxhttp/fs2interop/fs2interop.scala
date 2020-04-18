package be.wegenenverkeer.rxhttp

import java.nio.charset.Charset

import be.wegenenverkeer.rxhttp
import be.wegenenverkeer.rxhttp.ClientRequest
import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient
import cats.effect.{ConcurrentEffect, ContextShift}
import fs2.Stream
import fs2.interop.reactivestreams._

/**
  * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
  */
package object fs2interop {

  implicit class RxJavaHttpClientOps(client: RxJavaHttpClient) {

    def stream[F[_]:ConcurrentEffect](request: ClientRequest): Stream[F, ServerResponseElement]  =
      client.executeObservably(request).toStream()

    def streamDechunked[F[_]:ConcurrentEffect](request: ClientRequest, separator: String, charset: Charset) : Stream[F, String] =
      client.executeAndDechunk(request, separator, charset).toStream()


    def streamDechunked[F[_]:ConcurrentEffect](request: ClientRequest, separator: String) : Stream[F, String] =
      client.executeAndDechunk(request, separator).toStream()

    def stream[F[_]: ConcurrentEffect, A](req: ClientRequest, transform: Array[Byte] => A) : Stream[F, A] =
      client.executeObservably(req, (t: Array[Byte]) => transform(t)).toStream()

    //TODO return an async in stead of a (Completable)Future for a complete response
  }

}

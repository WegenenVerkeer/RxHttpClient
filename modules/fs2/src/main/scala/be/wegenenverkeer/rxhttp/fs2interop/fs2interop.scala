package be.wegenenverkeer.rxhttp

import java.nio.charset.Charset

import scala.jdk.FunctionConverters._
import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient
import cats.effect.{Async, ConcurrentEffect}
import fs2.Stream
import fs2.interop.reactivestreams._
import org.asynchttpclient.Response

import scala.util.Try

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

    def execute[F[_]: Async, A](req: ClientRequest, tr: ServerResponse => A): F[A] = {
      def attemptTransform(resp: Response) : Either[Throwable, A] =
        Try{ tr(ServerResponse.wrap(resp)) }.toEither
      Async[F].async{ cb =>
        client.inner().executeRequest(req.unwrap()).toCompletableFuture
          .whenComplete( (a, t)  =>
            if (t==null) cb( attemptTransform(a)) else cb(Left(t)) )
      }
    }


  }

}

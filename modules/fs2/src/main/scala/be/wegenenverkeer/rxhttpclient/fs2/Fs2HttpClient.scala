package be.wegenenverkeer.rxhttpclient.fs2

import java.nio.charset.Charset
import java.util

import be.wegenenverkeer.rxhttpclient.{ClientRequest, ClientRequestBuilder, RequestSigner, ServerResponse, ServerResponseElement}
import be.wegenenverkeer.rxhttpclient.rxjava.RxJavaHttpClient
import cats.effect.{Async, ConcurrentEffect}
import fs2.Stream
import fs2.interop.reactivestreams.fromPublisher
import org.asynchttpclient.Response

import scala.util.Try

/**
 * Created by Karel Maesen, Geovise BVBA on 20/04/2020.
 */
case class Fs2HttpClient(client: RxJavaHttpClient) extends Fs2HttpApi {

  def stream[F[_] : ConcurrentEffect](request: ClientRequest): Stream[F, ServerResponseElement] =
    fromPublisher[F, ServerResponseElement](client.executeObservably(request))

  def streamDechunked[F[_] : ConcurrentEffect](request: ClientRequest, separator: String, charset: Charset): Stream[F, String] =
    fromPublisher[F, String](client.executeAndDechunk(request, separator, charset))

  def streamDechunked[F[_] : ConcurrentEffect](request: ClientRequest, separator: String): Stream[F, String] =
    fromPublisher[F, String](client.executeAndDechunk(request, separator))

  def stream[F[_] : ConcurrentEffect, A](req: ClientRequest, transform: Array[Byte] => A): Stream[F, A] =
    fromPublisher[F, A](client.executeObservably(req, (t: Array[Byte]) => transform(t)))

  def streamBytes[F[_] : ConcurrentEffect](req: ClientRequest): Stream[F, Array[Byte]] =
    stream[F, Array[Byte]](req, identity)

  def execute[F[_] : Async, A](req: ClientRequest, tr: ServerResponse => A): F[A] = {
    def attemptTransform(resp: Response): Either[Throwable, A] =
      Try {
        tr(ServerResponse.wrap(resp))
      }.toEither

    Async[F].async { cb =>
      client.inner().executeRequest(req.unwrap()).toCompletableFuture
        .whenComplete((a, t) =>
          if (t == null) cb(attemptTransform(a)) else cb(Left(t)))
    }
  }

  def requestBuilder: ClientRequestBuilder = client.requestBuilder

  def requestSigners: util.List[RequestSigner] = client.getRequestSigners

}


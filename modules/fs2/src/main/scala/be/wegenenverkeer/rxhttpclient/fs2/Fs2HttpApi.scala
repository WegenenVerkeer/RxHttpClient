package be.wegenenverkeer.rxhttpclient.fs2

import java.nio.charset.Charset
import java.util

import be.wegenenverkeer.rxhttpclient._
import cats.effect.{Async, ConcurrentEffect}
import _root_.fs2.Stream

/**
 * Created by Karel Maesen, Geovise BVBA on 20/04/2020.
 */
trait Fs2HttpApi {

  def stream[F[_] : ConcurrentEffect](request: ClientRequest): Stream[F, ServerResponseElement]

  def streamDechunked[F[_] : ConcurrentEffect](request: ClientRequest, separator: String, charset: Charset): Stream[F, String]

  def streamDechunked[F[_] : ConcurrentEffect](request: ClientRequest, separator: String): Stream[F, String]

  def stream[F[_] : ConcurrentEffect, A](req: ClientRequest, transform: Array[Byte] => A): Stream[F, A]

  def execute[F[_] : Async, A](req: ClientRequest, tr: ServerResponse => A): F[A]

  def requestBuilder: ClientRequestBuilder

  def requestSigners: util.List[RequestSigner]

}

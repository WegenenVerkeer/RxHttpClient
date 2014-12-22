package be.wegenenverkeer.rxhttpclient.scala

import be.wegenenverkeer.rxhttp.{ServerResponse, ServerResponseElement, ClientRequest, RxHttpClient}
import rx.lang.scala.Observable

/**
 * Implicit conversion of RxHttpClient which adds methods that return {@code rx.lang.scala.Observables}.
 *
 * Created by Karel Maesen, Geovise BVBA on 22/12/14.
 */
object ImplicitConversions {

  import rx.lang.scala.JavaConversions._
  import java.util.function.{ Function ⇒ JFunction}


  private def toJavaFunction[A, B](f: A => B) : JFunction[A,B]= new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }

  case class RichRxHttpClient(inner: RxHttpClient) {

    def executeRequest[T](req: ClientRequest, transform : Array[Byte] => T) : Observable[T] =
      inner.executeRequest(req, toJavaFunction(transform))


    def executeRequest(req: ClientRequest) : Observable[ServerResponseElement] =
      inner.executeRequest(req)


    def executeToCompletion[T](req: ClientRequest, transform: ServerResponse => T) : Observable[T] =
      inner.executeToCompletion(req, toJavaFunction(transform))

  }

  implicit def toRichRxHttpClient(client : RxHttpClient ) : RichRxHttpClient = new RichRxHttpClient(client)
  implicit def toRxHttpClient(client : RichRxHttpClient ) : RxHttpClient = client.inner

}

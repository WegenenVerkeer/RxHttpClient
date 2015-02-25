package be.wegenenverkeer.rxhttp.scala

import java.util.concurrent.CompletionStage
import java.util.function.BiConsumer

import be.wegenenverkeer.rxhttp.{ServerResponse, ServerResponseElement, ClientRequest, RxHttpClient => JRxHttpClient}
import rx.lang.scala.Observable

import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Success}
/**
 * Implicit conversion of RxHttpClient which adds methods that return {@code rx.lang.scala.Observables}.
 *
 * Created by Karel Maesen, Geovise BVBA on 22/12/14.
 */
object ImplicitConversions {

  import rx.lang.scala.JavaConversions.toScalaObservable
  import java.util.function.{ Function ⇒ JFunction}

  private def toJavaFunction[A, B](f: A => B) : JFunction[A,B]= new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }

  private def fromJavaFuture[B](jfuture: CompletionStage[B]) : Future[B] = {
    val p = Promise[B]()
    val consumer = new BiConsumer[B, Throwable] {
      override def accept(t: B, u: Throwable): Unit = p.complete(Success(t))
    }
    jfuture.whenComplete( consumer )
    p.future
  }

  trait JavaClientWrapper {
    val inner: JRxHttpClient
    def asScala : RxHttpClient = new RxHttpClient(inner)
  }

  class RxHttpClient(val inner: JRxHttpClient) {

    def execute[T](req: ClientRequest, transform: ServerResponse => T) : Future[T] =
          fromJavaFuture(inner.execute[T](req, toJavaFunction(transform)))

    def executeObservably[T](req: ClientRequest, transform : Array[Byte] => T) : Observable[T] =
      toScalaObservable(inner.executeObservably(req, toJavaFunction(transform)))


    def executeObservably(req: ClientRequest) : Observable[ServerResponseElement] =
      toScalaObservable(inner.executeObservably(req))


    def executeToCompletion[T](req: ClientRequest, transform: Function[ServerResponse,T]): Observable[T] =
      toScalaObservable(inner.executeToCompletion(req, toJavaFunction(transform)))

  }

  implicit def wrap( c : JRxHttpClient) = new JavaClientWrapper{
    val inner = c
  }

  implicit def unwrap(client: RxHttpClient) : JRxHttpClient = client.inner

}


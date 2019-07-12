package be.wegenenverkeer.rxhttp.scala

import java.nio.charset.Charset
import java.util.concurrent.CompletionStage
import java.util.function.BiConsumer

import be.wegenenverkeer.rxhttp.{ClientRequest, ServerResponse, ServerResponseElement, RxHttpClient => JRxHttpClient}
import io.reactivex.BackpressureStrategy
import org.reactivestreams.Publisher

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}


class RxHttpClient(val inner: JRxHttpClient) {

  import java.util.function.{ Function ⇒ JFunction}

  private def toJavaFunction[A, B](f: A => B) : JFunction[A,B]= (a: A) => f(a)

  private def fromJavaFuture[B](jfuture: CompletionStage[B]) : Future[B] = {
    val p = Promise[B]()

    val consumer = new BiConsumer[B, Throwable] {
      override def accept(v: B, t: Throwable): Unit =
        if (t == null) p.complete(Success(v))
        else p.complete(Failure(t))
    }

    jfuture.whenComplete( consumer )
    p.future
  }

  def close() : Unit = this.inner.close()

  def execute[T](req: ClientRequest, transform: ServerResponse => T) : Future[T] =
    fromJavaFuture(inner.execute[T](req, toJavaFunction(transform)))

  def executeObservably[T](req: ClientRequest, transform : Array[Byte] => T) : Publisher[T] =
    inner.executeObservably(req, toJavaFunction(transform)).toFlowable(BackpressureStrategy.BUFFER)


  def executeObservably(req: ClientRequest) : Publisher[ServerResponseElement] =
    inner.executeObservably(req).toFlowable(BackpressureStrategy.BUFFER)


  def executeToCompletion[T](req: ClientRequest, transform: Function[ServerResponse,T]): Publisher[T] =
    inner.executeToCompletion(req, toJavaFunction(transform)).toFlowable(BackpressureStrategy.BUFFER)

  def executeAndDechunk[String](req: ClientRequest, separator: String, charset : Charset = Charset.forName("UTF8")): Publisher[String] =
    inner.executeAndDechunk(req, separator.asInstanceOf[java.lang.String], charset).map( _ .asInstanceOf[String])

}


/**
 * Implicit conversion of RxHttpClient which adds methods that return Reactive Stream Publishers.
 *
 * Created by Karel Maesen, Geovise BVBA on 22/12/14.
 */
object ImplicitConversions {


  trait JavaClientWrapper {
    val inner: JRxHttpClient
    def asScala : RxHttpClient = new RxHttpClient(inner)
  }


  implicit def wrap( c : JRxHttpClient) : JavaClientWrapper = new JavaClientWrapper{
    val inner : JRxHttpClient = c
  }

  implicit def unwrap(client: RxHttpClient) : JRxHttpClient = client.inner

}
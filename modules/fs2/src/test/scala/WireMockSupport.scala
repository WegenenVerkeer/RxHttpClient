import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.specs2.mutable.BeforeAfter
import org.specs2.specification.BeforeAfterEach

/**
 * Created by Karel Maesen, Geovise BVBA on 20/04/2020.
 */
trait WireMockSupport extends BeforeAfterEach {

  val REQUEST_TIME_OUT = 5000
  val DEFAULT_TIME_OUT: Int = REQUEST_TIME_OUT * 5

  val server: WireMockServer = new WireMockServer(options()
      .dynamicPort()
      .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE))

  private[this] var rx: RxJavaHttpClient = null

  def client: RxJavaHttpClient = rx

  def before: Unit = {
    server.start()
    rx = new RxJavaHttpClient.Builder()
      .setRequestTimeout(REQUEST_TIME_OUT)
      .setMaxConnections(3)
      .setAccept("application/json")
      .setBaseUrl("http://localhost:" + port)
      .build();
  }

  def after(): Unit = {
    println("Stopping server")
    server.stop()
    rx.close()
  }

  protected def port: Int = {
    println(s"Server reported as: ${server.port()}")
    server.port()
  }

}

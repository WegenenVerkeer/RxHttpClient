package be.wegenenverkeer.designtests


import be.wegenenverkeer.rxhttp.{ClientRequest, RxHttpClient}
import be.wegenenverkeer.rxhttpclient.scala.ImplicitConversions
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._

import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.specs2.mutable.{Before, After, Specification}
import rx.lang.scala.Observable


/**
 * Created by Karel Maesen, Geovise BVBA on 22/12/14.
 */


class RxHTTPClientGetSpecs extends Specification {



  "The GET request " should {

    "return scala Observable with ImplicitConversions imported" in new UsingMockServer {

      val expectBody: String = "{ 'contacts': [1,2,3] }"

      stubFor(get(urlPathEqualTo("/contacts?q=test"))
        .withQueryParam("q", com.github.tomakehurst.wiremock.client.WireMock.equalTo("test"))
        .withHeader("Accept", com.github.tomakehurst.wiremock.client.WireMock.equalTo("application/json"))
        .willReturn(aResponse.withFixedDelay(REQUEST_TIME_OUT / 3)
        .withStatus(200).withHeader("Content-Type", "application/json")
        .withBody(expectBody)))

      import ImplicitConversions._

      val req: ClientRequest = client.requestBuilder()
        .setMethod("GET")
        .setUrlRelativetoBase("/contacts")
        .addQueryParam("q", "test")
        .build()

      val observable: Observable[String] = client.executeRequest(req, (bytes: Array[Byte]) => new String(bytes))

      val response = observable
        .toBlocking
        .singleOption

       response must beSome(expectBody)
    }

  }

}

trait UsingMockServer extends After  {


  val REQUEST_TIME_OUT = 5000

  val port: Int = 8089

  val client = new RxHttpClient.Builder()
    .setRequestTimeout(REQUEST_TIME_OUT)
    .setMaxConnections(10)
    .setAccept("application/json")
    .setBaseUrl("http://localhost:" + port)
    .build

  import com.github.tomakehurst.wiremock.client.WireMock._

  val server = new WireMockServer(wireMockConfig.port(port))
  server.start()

  configureFor("localhost", port)

  def after = {server.shutdown(); Thread.sleep(1000)}
}
package be.wegenenverkeer.designtests


import be.wegenenverkeer.rxhttp.scala.ImplicitConversions._
import be.wegenenverkeer.rxhttp.{ClientRequest, RxHttpClient, ServerResponse}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.specs2.mutable.{After, Specification}
import rx.lang.scala.Observable
import rx.lang.scala.observers.TestSubscriber
import rx.observers.TestObserver

import scala.concurrent.{Await, Future}



/**
 * Created by Karel Maesen, Geovise BVBA on 22/12/14.
 */


class RxHTTPClientGetSpecs extends Specification {

    import scala.concurrent.duration._
    sequential

    "The GET request " should {

      "return scala Observable with executeObservably" in new UsingMockServer {
        val expectBody: String = "{ 'contacts': [1,2,3] }"

        stubFor(get(urlPathEqualTo("/contacts"))
          .withQueryParam("q", com.github.tomakehurst.wiremock.client.WireMock.equalTo("test"))
          .withHeader("Accept", com.github.tomakehurst.wiremock.client.WireMock.equalTo("application/json"))
          .willReturn(aResponse.withFixedDelay(REQUEST_TIME_OUT / 3)
          .withStatus(200).withHeader("Content-Type", "application/json")
          .withBody(expectBody)))


        val req: ClientRequest = client.requestBuilder()
          .setMethod("GET")
          .setUrlRelativetoBase("/contacts")
          .addQueryParam("q", "test")
          .build()

        val observable: Observable[String] = client.executeObservably(req, (bytes: Array[Byte]) => new String(bytes))

        val response = observable
          .toBlocking
          .singleOption

        response must beSome(expectBody)
      }


      "return scala Future with execute" in new UsingMockServer {




        val expectBody: String = "{ 'contacts': [1,2,3] }"

        stubFor(get(urlPathEqualTo("/contacts"))
          .withQueryParam("q", com.github.tomakehurst.wiremock.client.WireMock.equalTo("test"))
          .withHeader("Accept", com.github.tomakehurst.wiremock.client.WireMock.equalTo("application/json"))
          .willReturn(aResponse.withFixedDelay(20)
          .withStatus(200).withHeader("Content-Type", "application/json")
          .withBody(expectBody)))


        val req: ClientRequest = client.requestBuilder()
          .setMethod("GET")
          .setUrlRelativetoBase("/contacts")
          .addQueryParam("q", "test")
          .build()

        val future : Future[String] = client.execute(req, (resp: ServerResponse) => resp.getResponseBody)

        val response = Await.result(future, 2.seconds)

        response must_== expectBody
      }

      "return scala Future with executeCompletely" in new UsingMockServer {

        val expectBody: String = "{ 'contacts': [1,2,3] }"

        stubFor(get(urlPathEqualTo("/contacts"))
          .withQueryParam("q", com.github.tomakehurst.wiremock.client.WireMock.equalTo("test"))
          .withHeader("Accept", com.github.tomakehurst.wiremock.client.WireMock.equalTo("application/json"))
          .willReturn(aResponse.withFixedDelay(20)
          .withStatus(200).withHeader("Content-Type", "application/json")
          .withBody(expectBody)))


        val req: ClientRequest = client.requestBuilder()
          .setMethod("GET")
          .setUrlRelativetoBase("/contacts")
          .addQueryParam("q", "test")
          .build()


        val observable: Observable[String] = client.executeToCompletion(req, (resp: ServerResponse) => resp.getResponseBody)

        val response = observable
          .toBlocking
          .singleOption

        response must beSome(expectBody)
      }

      "return delimited evens (Strings)" in new UsingMockServer {
        stubFor(get(urlPathEqualTo("/sse"))
          .willReturn(
            aResponse
              .withBodyFile("sse-output.txt")
              .withChunkedDribbleDelay(50, 30)))

        val request: ClientRequest = client.requestBuilder.setMethod("GET").setUrlRelativetoBase("/sse").build

        val observable = client.executeAndDechunk(request, "\n")

        val testSub = TestSubscriber[String]
        observable.subscribe(testSub)

        testSub.awaitTerminalEvent(1.seconds)

        testSub.getOnNextEvents.size must beEqualTo(10)

      }
    }

}



trait UsingMockServer extends After  {


  val REQUEST_TIME_OUT = 5000

  //we take a different port then in java-client module, because tests unfortunately continue to overlap with java client module
  val port: Int = 8088

  val client = new RxHttpClient.Builder()
    .setRequestTimeout(REQUEST_TIME_OUT)
    .setMaxConnections(10)
    .setAccept("application/json")
    .setBaseUrl("http://localhost:" + port)
    .build.asScala

  import com.github.tomakehurst.wiremock.client.WireMock._

  val server = new WireMockServer(wireMockConfig.port(port).useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE))
  server.start()

  configureFor("localhost", port)

  def after = {
    server.shutdown(); Thread.sleep(1000)
  }
}
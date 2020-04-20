import be.wegenenverkeer.rxhttp.fs2.Implicits._
import cats.effect.{ContextShift, IO}
import scala.concurrent.ExecutionContext

/**
 * Created by Karel Maesen, Geovise BVBA on 20/04/2020.
 */
class Fs2InteropSpecification extends org.specs2.mutable.Specification
  with WireMockSupport {

  sequential

  "The implicits object" should {

    "provide an a FS2-compliant streaming interface" in {
      val expectBody = "{ 'contacts': [1,2,3] }"

      implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

      stub(expectBody)
      val path = "/contacts"
      val request = client.requestBuilder.setMethod("GET").setUrlRelativetoBase(path).addQueryParam("q", "test").build


      val response = client.fs2HttpApi.stream[IO, String](request, b => new String(b))
      val output = response.compile.toVector.unsafeRunSync()

      output(0) must_== expectBody

    }

    "provide a Cats-Effect Aƒsync compliant interface" in {

      val expectBody = "{ 'contacts': [1,2,3] }"

      stub(expectBody)
      //set up use case

      import scala.concurrent.ExecutionContext
      implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

      val path = "/contacts"
      val request = client.requestBuilder.setMethod("GET").setUrlRelativetoBase(path).addQueryParam("q", "test").build

      val resp = client.fs2HttpApi.execute[IO, String](request, sr => sr.getResponseBody)
      val output = resp.unsafeRunSync()

      output must_== expectBody
    }
  }

  def stub(expectBody: String) = {
    import com.github.tomakehurst.wiremock.client.{WireMock => wm}
    server.stubFor(wm.get(wm.urlPathEqualTo("/contacts"))
      .withQueryParam("q", wm.equalTo("test"))
      .withHeader("Accept", wm.equalTo("application/json"))
      .willReturn(wm.aResponse.withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody(expectBody)
      )
    )
  }


}

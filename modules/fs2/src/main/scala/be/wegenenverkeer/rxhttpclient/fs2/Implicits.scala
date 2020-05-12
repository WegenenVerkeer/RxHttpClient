
package be.wegenenverkeer.rxhttpclient.fs2

import be.wegenenverkeer.rxhttpclient.rxjava.RxJavaHttpClient

/**
 * Created by Karel Maesen, Geovise BVBA on 20/04/2020.
 */
object Implicits {

  implicit class RxJavaHttpClientOps(client: RxJavaHttpClient) {
    def fs2HttpApi: Fs2HttpApi = new Fs2HttpClient(client)
  }
}


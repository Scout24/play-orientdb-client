package embeddedorient

import play.api.libs.ws.{WSClientConfig, WSClient}
import play.api.libs.ws.ning.{NingWSClientConfig, NingWSClient, NingAsyncHttpClientConfigBuilder}

object TestHTTPClient {
  private val wSClientConfig =  new WSClientConfig(useProxyProperties = false)
  private val clientConfig: NingWSClientConfig = new NingWSClientConfig(wsClientConfig = wSClientConfig)
  private val builder = new NingAsyncHttpClientConfigBuilder(clientConfig)
  val wsClient : WSClient = new NingWSClient(builder.build())
}


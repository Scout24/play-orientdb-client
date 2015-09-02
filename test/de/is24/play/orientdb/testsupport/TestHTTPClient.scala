package de.is24.play.orientdb.testsupport

import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient, NingWSClientConfig}
import play.api.libs.ws.{WSClient, WSClientConfig}

object TestHTTPClient {
  private val wSClientConfig =  new WSClientConfig(useProxyProperties = false)
  private val clientConfig: NingWSClientConfig = new NingWSClientConfig(wsClientConfig = wSClientConfig)
  private val builder = new NingAsyncHttpClientConfigBuilder(clientConfig)
  val wsClient : WSClient = new NingWSClient(builder.build())
}


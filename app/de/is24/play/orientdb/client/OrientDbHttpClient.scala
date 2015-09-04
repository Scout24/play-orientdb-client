package de.is24.play.orientdb.client

import akka.event.slf4j.SLF4JLogging
import de.is24.play.orientdb.{BatchOperation, OrientDbQuery}
import play.api.libs.json._
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}
import OrientProtocol._
import scala.concurrent.{ExecutionContext, Future}

class OrientDbHttpClient(config: OrientClientConfig, wsClient: WSClient)(implicit ec: ExecutionContext) extends SLF4JLogging {


  private val orientDbCommandUrl = s"${config.url}/command/${config.database}/sql"

  private val orientDbBatchUrl = s"${config.url}/batch/${config.database}"

  private def orientDbFunctionUrl(name: String) = s"${config.url}/function/${config.database}/$name"


  def select[T: Reads](orientDbQuery: OrientDbQuery): Future[Seq[T]] = {
    command(orientDbQuery)
      .flatMap { response =>
      val json = response.json
      log.debug("Received orient body {}", json)
      (json \ "result").validate[Seq[T]] match {
        case JsSuccess(result, _) =>
          Future.successful(result)
        case JsError(e) =>
          Future.failed(new RuntimeException(s"Orient db call result has invalid body: $e"))
      }
    }
  }


  def selectJson(orientDbQuery: OrientDbQuery): Future[Seq[JsValue]] = {
    command(orientDbQuery)
      .flatMap { response =>
      val json = response.json
      log.debug("Received orient body {}", json)
      (json \ "result").validate[JsArray] match {
        case JsSuccess(result, _) =>
          Future.successful(result.value)
        case JsError(e) =>
          Future.failed(new RuntimeException(s"Orient db call result has invalid body: $e"))
      }
    }
  }

  def command(orientDbQuery: OrientDbQuery): Future[WSResponse] = {
    val request: WSRequest = wsClient
      .url(orientDbCommandUrl)
      .withAuth(config.userName, config.password, WSAuthScheme.BASIC)
      .withMethod("POST")
      .withBody(orientDbQuery.query)
    request
      .execute()
      .flatMap(handleErrorResponse(request))
  }

  def executeBatch(batchOperation: BatchOperation): Future[JsValue] = {
    val request: WSRequest = wsClient
      .url(orientDbBatchUrl)
      .withAuth(config.userName, config.password, WSAuthScheme.BASIC)
      .withMethod("POST")
      .withBody(Json.toJson(batchOperation))
    request
      .execute()
      .flatMap(handleErrorResponse(request))
      .map { response =>
      response.json
    }
  }

  def createDatabase() = {
    val createDatabaseUrl = s"${config.url}/database/${config.database}/memory/graph"

    val request: WSRequest = wsClient
      .url(createDatabaseUrl)
      .withAuth(config.userName, config.password, WSAuthScheme.BASIC)
      .withMethod("POST")
    request
      .execute()
      .flatMap(handleErrorResponse(request))
      .map(_.json)
  }

  def callFunction(name: String, parameters: Map[String, Any] = Map.empty): Future[JsValue] = {
    val serializedParameters = JsObject(parameters.map {
      case (parameterName, numericValue: Number) => parameterName -> JsNumber(BigDecimal.valueOf(numericValue.doubleValue))
      case (parameterName, booleanValue: Boolean) => parameterName -> JsBoolean(booleanValue)
      case (parameterName, anyValue: Any) => parameterName -> JsString(anyValue.toString)
    })

    val request: WSRequest = wsClient
      .url(orientDbFunctionUrl(name))
      .withAuth(config.userName, config.password, WSAuthScheme.BASIC)
      .withMethod("POST")
      .withBody(serializedParameters)
    request
      .execute()
      .flatMap(handleErrorResponse(request))
      .map(_.json)
  }

  private def handleErrorResponse(request: WSRequest)(response: WSResponse): Future[WSResponse] = {
    if (response.status >= 300) createErrorResponse(request, response)
    else Future.successful(response)
  }

  private def createErrorResponse[T](request: WSRequest, res: WSResponse): Future[Nothing] = {
    val method = request.method.toUpperCase
    log.warn(s"$method '${request.url}' failed with status ${res.status}. Response body:\n${res.body}")

    Future.failed(new OrientRequestFailedException(s"$method ${request.url}' failed with status ${res.status}"))
  }
}

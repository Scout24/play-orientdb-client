package de.is24.play.orientdb.client

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import de.is24.play.orientdb.{BatchOperation, OrientDbQuery}
import play.api.libs.json._
import OrientProtocol._
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.immutable._
import scala.util.{Failure, Success}
import scala.concurrent.duration._

class OrientDbHttpClient(config: OrientClientConfig)(implicit actorSystem: ActorSystem) extends SLF4JLogging {

  private implicit val ec = actorSystem.dispatcher

  private implicit val materializer = ActorMaterializer()

  private val JsonContentType = MediaTypes.`application/json`

  private val orientDbCommandUrl = s"${config.url}/command/${config.database}/"

  private val orientDbBatchUrl = s"${config.url}/batch/${config.database}"

  private def orientDbFunctionUrl(name: String) = s"${config.url}/function/${config.database}/$name"

  private val authorization = headers.Authorization(BasicHttpCredentials(config.userName, config.password))

  private val http = Http()


  def select[T: Reads](orientDbQuery: OrientDbQuery): Future[Seq[T]] = {
    command(orientDbQuery)
      .flatMap { responseJson =>
      log.debug("Received orient body {}", responseJson)
      (responseJson \ "result").validate[Seq[T]] match {
        case JsSuccess(result, _) =>
          Future.successful(result)
        case JsError(e) =>
          Future.failed(new RuntimeException(s"Orient db call result has invalid body: $e"))
      }
    }
  }

  def selectJson(orientDbQuery: OrientDbQuery): Future[Seq[JsValue]] = {
    select[JsValue](orientDbQuery)
  }

  def command(orientDbQuery: OrientDbQuery, timeout: FiniteDuration = 30.seconds): Future[JsValue] = {
    val entity = HttpEntity(orientDbQuery.query)
    log.debug("Execute command {}", orientDbQuery)
    val request: HttpRequest = HttpRequest(
      uri = orientDbCommandUrl + orientDbQuery.language,
      entity = entity,
      method = HttpMethods.POST,
      headers = Seq[HttpHeader](authorization))

    executeRequest(request, timeout)
  }

  private def executeRequest(request: HttpRequest, timeout: FiniteDuration): Future[JsValue] = {
    Source
      .single(request -> 42)
      .via(http.superPool[Int]())
      .completionTimeout(timeout)
      .map(_._1)
      .runWith(Sink.head)
      .flatMap {
        case Success(r) => Future.successful(r)
        case Failure(e) => Future.failed(e)
      }
      .flatMap(handleErrorResponse(request))
      .flatMap(unmarshalResponse)
  }

  def executeBatch(batchOperation: BatchOperation, timeout: FiniteDuration = 30.seconds): Future[JsValue] = {
    val entity = HttpEntity(JsonContentType, Json.stringify(Json.toJson(batchOperation)))
    val request: HttpRequest = HttpRequest(uri = orientDbBatchUrl, entity = entity, method = HttpMethods.POST, headers = Seq[HttpHeader](authorization))
    log.debug("Execute batch {}", batchOperation)
    executeRequest(request, timeout)
  }

  def createDatabase(timeout: FiniteDuration = 30.seconds): Future[JsValue] = {
    val createDatabaseUrl = s"${config.url}/database/${config.database}/memory/graph"
    val request: HttpRequest = HttpRequest(
      uri = createDatabaseUrl,
      entity = HttpEntity.empty(ContentTypes.NoContentType),
      method = HttpMethods.POST,
      headers = Seq[HttpHeader](authorization)
    )
    executeRequest(request, timeout)
  }

  def callFunction(name: String, parameters: Map[String, Any] = Map.empty, timeout: FiniteDuration = 30.seconds): Future[JsValue] = {
    val serializedParameters = JsObject(parameters.map {
      case (parameterName, numericValue: Number) => parameterName -> JsNumber(BigDecimal.valueOf(numericValue.doubleValue))
      case (parameterName, booleanValue: Boolean) => parameterName -> JsBoolean(booleanValue)
      case (parameterName, anyValue: Any) => parameterName -> JsString(anyValue.toString)
    })

    val entity = HttpEntity(JsonContentType, Json.stringify(Json.toJson(serializedParameters)))
    val request: HttpRequest = HttpRequest(
      uri = orientDbFunctionUrl(name),
      entity = entity,
      method = HttpMethods.POST,
      headers = Seq[HttpHeader](authorization)
    )

    executeRequest(request, timeout)
  }

  private def unmarshalResponse(response: HttpResponse): Future[JsValue] = {
    response.status match {
      case StatusCodes.NoContent =>
        Unmarshal(response.entity).to[String].map(JsString.apply)
      case _ =>
        import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport._
        Unmarshal(response.entity).to[JsValue]
    }
  }
  private def handleErrorResponse(request: HttpRequest)(response: HttpResponse): Future[HttpResponse] = {
    if (response.status.isFailure()) createErrorResponse(request, response)
    else Future.successful(response)
  }

  private def createErrorResponse[T](request: HttpRequest, res: HttpResponse): Future[Nothing] = {
    val method = request.method.toString().toUpperCase
    Unmarshal(res.entity).to[String].flatMap { errorBody =>
      log.warn(s"$method '${request.uri}' failed with status ${res.status} and body '$errorBody'")
      Future.failed(new OrientRequestFailedException(s"$method ${request.uri}' failed with status ${res.status}"))
    }
  }
}

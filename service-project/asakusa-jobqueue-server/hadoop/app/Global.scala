/**
 * Copyright 2012 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File

import com.asakusafw.jobqueue.core._
import com.typesafe.config._

import akka.actor.Props
import models._
import play.api._
import play.api.http.Status
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json._
import play.api.mvc._
import play.api.mvc.Results._

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
object Global extends GlobalSettings {

  /**
   * @param app
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override def onStart(app: Application) {
    val asakusaHome = Settings.asakusaHome
    val config = Settings.config

    val nrWorker = Settings.nrWorker
    val duration = Settings.completedJobDuration
    val hadoopLogDir = try {
      Some(Settings.hadoopLogDir)
    } catch {
      case e: ConfigException =>
        Logger.warn(e.getMessage)
        None
    }
    val jobQueueAskTimeout = Settings.jobQueueAskTimeout

    Akka.system(app).actorOf(Props(new HadoopJobQueue(nrWorker, duration, new File(asakusaHome), hadoopLogDir)), "jobqueue")

    Logger.info(
      "JobQueue Server started. (ASAKUSA_HOME: " + asakusaHome + ", core.worker: " + nrWorker + ", " +
        "hadoop.log.dir: " + hadoopLogDir + ", jobQueueAskTimeout: " + jobQueueAskTimeout + ")")
  }

  /**
   * @param request
   * @param error
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override def onBadRequest(request: RequestHeader, error: String) = {
    BadRequest(toJson(Map(
      "status" -> Error.name,
      "errorCode" -> Status.BAD_REQUEST.toString,
      "errorMessage" -> error)))
  }

  /**
   * @param request
   * @param ex
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override def onError(request: RequestHeader, ex: Throwable) = {
    InternalServerError(toJson(Map(
      "status" -> Error.name,
      "errorCode" -> Status.INTERNAL_SERVER_ERROR.toString,
      "errorMessage" -> ex.getMessage)))
  }

  /**
   * @param request
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override def onHandlerNotFound(request: RequestHeader) = {
    NotFound(toJson(Map(
      "status" -> Error.name,
      "errorCode" -> Status.NOT_FOUND.toString,
      "errorMessage" -> ("The requested URL " + request.path + " was not found on this server."))))
  }

}

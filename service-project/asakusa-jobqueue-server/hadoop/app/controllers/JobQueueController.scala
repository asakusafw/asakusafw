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
package controllers

import java.text.MessageFormat

import com.asakusafw.jobqueue.core._

import akka.dispatch._
import akka.pattern._
import models._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc._

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
object JobQueueController extends Controller {

  implicit lazy val Timeout = Settings.jobQueueAskTimeout

  def index = TODO

  /**
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  def register = Action(parse.json) { json =>
    try {
      val jobInfo = HadoopJobInfo(null,
        (json.body \ "batchId").asOpt[String].getOrElse(throw NoBatchIdException),
        (json.body \ "flowId").asOpt[String].getOrElse(throw NoFlowIdException),
        (json.body \ "executionId").asOpt[String].getOrElse(throw NoExecutionIdException),
        (json.body \ "phaseId").asOpt[String].getOrElse(throw NoPhaseIdException),
        (json.body \ "stageId").asOpt[String].getOrElse(throw NoStageIdException),
        (json.body \ "mainClass").asOpt[String].getOrElse(throw NoMainClassException),
        Initialized,
        None,
        ((json.body \ "arguments").asOpt[JsObject].map(_.value.map {
          case (key, value) => (key, value.as[String])
        }.toMap).getOrElse(Map.empty)),
        ((json.body \ "properties").asOpt[JsObject].map(_.value.map {
          case (key, value) => (key, value.as[String])
        }.toMap).getOrElse(Map.empty)),
        ((json.body \ "env").asOpt[JsObject].map(_.value.map {
          case (key, value) => (key, value.as[String])
        }.toMap).getOrElse(Map.empty)))

      val actor = Akka.system.actorFor("/user/jobqueue")
      val future = (actor ? REGISTER[HadoopJobInfo](jobInfo)).mapTo[JOB_INFO[HadoopJobInfo]]
      val registered = Await.result(future, Timeout.duration).jobInfo

      Ok(toJson(Map(
        "status" -> registered.status.name, "jrid" -> registered.jrid)))
    } catch { case e: HadoopJobQueueException => BadRequest(e.toJson) }
  }

  /**
   * @param jrid
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  def info(jrid: String) = Action {
    val actor = Akka.system.actorFor("/user/jobqueue")
    val future = (actor ? INFO(jrid)).mapTo[JOB_INFO_OPT[HadoopJobInfo]]
    val jobInfo = Await.result(future, Timeout.duration).jobInfo

    jobInfo.map { jobInfo =>
      Ok(toJson(Map(
        "status" -> jobInfo.status.name, "jrid" -> jobInfo.jrid) ++ jobInfo.exitCode.map("exitCode" -> _.toString)))
    }.getOrElse(NotFound(JobQueueNotFoundException.toJson))
  }

  /**
   * @param jrid
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  def execute(jrid: String) = Action {
    val actor = Akka.system.actorFor("/user/jobqueue")
    val future = (actor ? EXECUTE(jrid)).mapTo[JOB_INFO_OPT[HadoopJobInfo]]
    val jobInfo = Await.result(future, Timeout.duration).jobInfo

    jobInfo.map { jobInfo =>
      Ok(toJson(Map(
        "status" -> jobInfo.status.name, "jrid" -> jobInfo.jrid)))
    }.getOrElse(NotFound(JobQueueNotFoundException.toJson))
  }

  /**
   * @param jrid
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  def delete(jrid: String) = Action {
    val actor = Akka.system.actorFor("/user/jobqueue")
    val future = (actor ? DELETE(jrid)).mapTo[JOB_INFO_OPT[HadoopJobInfo]]
    val jobInfo = Await.result(future, Timeout.duration).jobInfo

    jobInfo.map { jobInfo =>
      jobInfo.status match {
        case Deleted => Ok(toJson(Map(
          "status" -> jobInfo.status.name, "jrid" -> jobInfo.jrid)))
        case _ => MethodNotAllowed(toJson(Map(
          "status" -> jobInfo.status.name,
          "jrid" -> jobInfo.jrid,
          "errorCode" -> METHOD_NOT_ALLOWED.toString,
          "errorMessage" -> MessageFormat.format("JobQueue ({0}) was already running.", jobInfo.jrid))))
      }
    }.getOrElse(NotFound(JobQueueNotFoundException.toJson))
  }

}

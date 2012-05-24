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
package models

import com.asakusafw.jobqueue.core._
import play.api.libs.json._
import play.api.http.Status

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
sealed trait HadoopJobQueueException extends Exception {

  val status = Error.name

  def toJson: JsValue
}

class BadRequestException extends HadoopJobQueueException {

  val errorCode = Status.BAD_REQUEST.toString

  def toJson = Json.toJson(Map(
    "status" -> status,
    "errorCode" -> errorCode))
}
object BadRequestException extends BadRequestException

class NoBatchIdException extends BadRequestException {

  override def toJson = Json.toJson(Map(
    "status" -> status,
    "errorCode" -> errorCode,
    "errorMessage" -> "No batchId specified."))
}
object NoBatchIdException extends NoBatchIdException

class NoFlowIdException extends BadRequestException {

  override def toJson = Json.toJson(Map(
    "status" -> status,
    "errorCode" -> errorCode,
    "errorMessage" -> "No flowId specified."))
}
object NoFlowIdException extends NoFlowIdException

class NoExecutionIdException extends BadRequestException {

  override def toJson = Json.toJson(Map(
    "status" -> status,
    "errorCode" -> errorCode,
    "errorMessage" -> "No executionId specified."))
}
object NoExecutionIdException extends NoExecutionIdException

class NoPhaseIdException extends BadRequestException {

  override def toJson = Json.toJson(Map(
    "status" -> status,
    "errorCode" -> errorCode,
    "errorMessage" -> "No phaseId specified."))
}
object NoPhaseIdException extends NoPhaseIdException

class NoStageIdException extends HadoopJobQueueException {

  override def toJson = Json.toJson(Map(
    "status" -> status,
    "errorCode" -> Status.BAD_REQUEST.toString,
    "errorMessage" -> "No stageId specified."))
}
object NoStageIdException extends NoStageIdException

class NoMainClassException extends BadRequestException {

  override def toJson = Json.toJson(Map(
    "status" -> status,
    "errorCode" -> errorCode,
    "errorMessage" -> "No mainClass specified."))
}
object NoMainClassException extends NoMainClassException

class JobQueueNotFoundException extends HadoopJobQueueException {

  def toJson = Json.toJson(Map(
    "status" -> status,
    "errorCode" -> Status.NOT_FOUND.toString,
    "errorMessage" -> "Specified jrid was not found."))
}
object JobQueueNotFoundException extends JobQueueNotFoundException

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
package com.asakusafw.jobqueue.core

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
sealed abstract class JobStatus(val name: String)

case object Initialized extends JobStatus("initialized")
case object Waiting extends JobStatus("waiting")
case object Running extends JobStatus("running")
case object Completed extends JobStatus("completed")
case object Error extends JobStatus("error")
case object Deleted extends JobStatus("deleted")

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
object JobStatus {

  /**
   * @param value
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  def valueOf(value: String) = {
    value match {
      case Initialized.name => Option(Initialized)
      case Waiting.name => Option(Waiting)
      case Running.name => Option(Running)
      case Completed.name => Option(Completed)
      case Error.name => Option(Error)
      case Deleted.name => Option(Deleted)
      case _ => None
    }
  }
}

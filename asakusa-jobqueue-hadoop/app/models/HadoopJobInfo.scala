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

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
case class HadoopJobInfo(
  jrid: String,
  batchId: String,
  flowId: String,
  executionId: String,
  phaseId: String,
  stageId: String,
  mainClass: String,
  status: JobStatus,
  exitCode: Option[Int] = None,
  arguments: Map[String, String] = Map.empty,
  properties: Map[String, String] = Map.empty,
  env: Map[String, String] = Map.empty) extends JobInfo[String, Int]

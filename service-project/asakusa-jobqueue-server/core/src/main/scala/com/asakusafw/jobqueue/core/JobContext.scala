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

import java.io._

import scala.sys.process._

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
case class JobContext[ID, EC, JI <: JobInfo[ID, EC]](
  jobInfo: JI,
  workingDirectory: File = new File("."),
  in: InputStream = new ByteArrayInputStream(Array()),
  out: OutputStream = new FilterOutputStream(stdout) { override def close() {} },
  err: OutputStream = new FilterOutputStream(stderr) { override def close() {} })

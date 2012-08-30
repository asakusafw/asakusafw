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

import java.io.File
import scala.sys.process._
import com.asakusafw.jobqueue.core._
import HadoopJobWorker._
import java.io.IOException

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
class HadoopJobWorker(asakusaHome: File) extends JobWorker[String, Int, HadoopJobInfo] {

  import HadoopJobWorker._

  lazy val sh = new File(asakusaHome.getAbsolutePath + JobQueueHadoopScript)

  lazy val existsShFile = sh.exists

  lazy val executable = sh.canExecute

  val ShFileNotExistsErrorCode = 127

  val ShFileNotExecutableErrorCode = 126

  private val RegexpEscapeArgument = """([=,\\])"""

  /**
   * @param jobContext
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  def run(jobContext: JobContext[String, Int, HadoopJobInfo]): Int = {
    if (!existsShFile) {
      ShFileNotExistsErrorCode
    } else if (!executable) {
      ShFileNotExecutableErrorCode
    } else {
      val process = Process(
        Seq(
          asakusaHome.getAbsolutePath + JobQueueHadoopScript,
          jobContext.jobInfo.mainClass,
          jobContext.jobInfo.batchId,
          jobContext.jobInfo.flowId,
          jobContext.jobInfo.executionId,
          jobContext.jobInfo.jrid,
          jobContext.jobInfo.arguments.map(kv =>
            kv._1.replaceAll(RegexpEscapeArgument, "\\\\$1") + "=" + kv._2.replaceAll(RegexpEscapeArgument, "\\\\$1")).mkString(",")) ++
          jobContext.jobInfo.properties.flatMap(kv => Seq("-D", kv._1 + "=" + kv._2)).toSeq,
        jobContext.workingDirectory.getAbsoluteFile,
        jobContext.jobInfo.env.toSeq: _*)
      process.run(new ProcessIO(
        in => try {
          BasicIO.transferFully(jobContext.in, in)
        } finally {
          try { jobContext.in.close() } finally { in.close() }
        },
        out => try {
          BasicIO.transferFully(out, jobContext.out)
        } finally {
          try { out.close() } finally { jobContext.out.close() }
        },
        err => try {
          BasicIO.transferFully(err, jobContext.err)
        } finally {
          try { err.close() } finally { jobContext.err.close() }
        })).exitValue()
    }
  }
}

object HadoopJobWorker {

  val JobQueueHadoopScript = "/jobqueue-hadoop/libexec/hadoop-execute.sh"

}

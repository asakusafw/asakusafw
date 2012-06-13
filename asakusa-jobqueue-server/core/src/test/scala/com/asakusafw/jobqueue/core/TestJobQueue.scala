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

import scala.collection.mutable

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
class TestJobQueue(val nrWorkers: Int, val completedJobDuration: Long) extends JobQueue[Int, String, TestJobInfo] {

  val map = mutable.Map(
    10 -> TestJobInfo(10, "value10", Completed, Some("0")),
    20 -> TestJobInfo(20, "value20", Initialized, None))

  def newWorker() = new JobWorker[Int, String, TestJobInfo] {

    def run(jobContext: JobContext[Int, String, TestJobInfo]) = {
      Thread.sleep(5000)
      jobContext.jobInfo.toString
    }
  }

  override def register(jobInfo: TestJobInfo) = {
    map += (1 -> TestJobInfo(1, jobInfo.value, Initialized, None))
    map(1)
  }

  override def info(jrid: Int) = {
    map.get(jrid)
  }

  override def execute(jrid: Int) = {
    map.get(jrid).map { jobInfo =>
      if (jobInfo.status == Initialized) {
        map += (jrid -> TestJobInfo(jrid, jobInfo.value, Waiting, jobInfo.exitCode))
        super.execute(jrid)
        map(jrid)
      } else jobInfo
    }
  }

  override def delete(jrid: Int) = {
    map.get(jrid).map { jobInfo =>
      if (jobInfo.status == Initialized || jobInfo.status == Completed || jobInfo.status == Error) {
        map -= jobInfo.jrid
        jobInfo
      } else jobInfo
    }
  }

  override def invoke(jobInfo: TestJobInfo) = {
    map += (jobInfo.jrid -> TestJobInfo(jobInfo.jrid, jobInfo.value, Running, None))
    workers ! INVOKE_WORKER(JobContext(map(jobInfo.jrid)))
    super.invoke(map(jobInfo.jrid))
  }

  override def complete(jobContext: JobContext[Int, String, TestJobInfo], exitCode: String) = {
    map += (jobContext.jobInfo.jrid ->
      TestJobInfo(jobContext.jobInfo.jrid, jobContext.jobInfo.value, Completed, Option(exitCode)))
    super.complete(
      JobContext[Int, String, TestJobInfo](
        map(jobContext.jobInfo.jrid), jobContext.workingDirectory, jobContext.in, jobContext.out, jobContext.err), exitCode)
  }

}

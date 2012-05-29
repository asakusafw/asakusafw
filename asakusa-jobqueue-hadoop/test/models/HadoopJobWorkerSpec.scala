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

import java.io._

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._

import com.asakusafw.jobqueue.core._

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
@RunWith(classOf[JUnitRunner])
class HadoopJobWorkerSpec extends Specification with Tags {

  object TestContext extends Before {

    /* (non-Javadoc)
     * @see org.specs2.specification.Before#before()
     */
    def before = {
      new File("test/asakusa" + HadoopJobWorker.JobQueueHadoopScript).setExecutable(true, false)
    }
  }

  "HadoopJobWorker" should {

    "run hadoop-execute.sh" in TestContext {
      val worker = new HadoopJobWorker(new File("test/asakusa"))
      val workingDirectory = new File("test")
      val in = new ByteArrayInputStream("stdin\n".getBytes())
      val out = new ByteArrayOutputStream()
      val err = new ByteArrayOutputStream()
      val exitCode = worker.run(JobContext(
        HadoopJobInfo("jrid", "batchId", "flowId", "executionId", "phaseId", "stageId", "mainClass", Running, None,
          Map("argument=1" -> "avalue=\\,1"), Map("prop1" -> "pvalue1", "prop2" -> "pvalue2"), Map("env1" -> "evalue1")),
        workingDirectory = workingDirectory, in = in, out = out, err = err))
      exitCode must equalTo(0)
      new String(out.toByteArray) must equalTo("HadoopWorkerJob Test\n" +
        "stdin\n" +
        "mainClass batchId flowId executionId jrid " +
        "argument\\=1=avalue\\=\\\\\\,1 " +
        "-D prop1=pvalue1 -D prop2=pvalue2\n")
      new String(err.toByteArray) must equalTo("env1: evalue1\n")
    } tag ("run")

    "doesn't run not executable sh file" in TestContext {
      new File("test/asakusa" + HadoopJobWorker.JobQueueHadoopScript).setExecutable(false, false)
      val worker = new HadoopJobWorker(new File("test/asakusa"))
      val exitCode = worker.run(JobContext(
        HadoopJobInfo("jrid", "batchId", "flowId", "executionId", "phaseId", "stageId", "mainClass", Running, None,
          Map("argument1" -> "avalue1"), Map("prop1" -> "pvalue1"), Map("env1" -> "evalue1"))))
      exitCode must equalTo(126)
    } tag ("notexecutable")

    "doesn't run not existing sh file" in TestContext {
      val worker = new HadoopJobWorker(new File("test/dosakusa"))
      val exitCode = worker.run(JobContext(
        HadoopJobInfo("jrid", "batchId", "flowId", "executionId", "phaseId", "stageId", "mainClass", Running, None,
          Map("argument1" -> "avalue1"), Map("prop1" -> "pvalue1"), Map("env1" -> "evalue1"))))
      exitCode must equalTo(127)
    } tag ("notexisting")
  } section ("HadoopJobWorker")

}

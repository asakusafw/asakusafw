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

import java.io.File

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._

import models._
import play.api.libs.json.Json._
import play.api.test._
import play.api.test.Helpers._

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
@RunWith(classOf[JUnitRunner])
class JobQueueControllerSpec extends Specification with Tags {

  object TestContext extends BeforeAfter {

    /* (non-Javadoc)
     * @see org.specs2.specification.Before#before()
     */
    def before = {
      new File("test/asakusa" + HadoopJobWorker.JobQueueHadoopScript).setExecutable(true)
      System.setProperty("ASAKUSA_HOME", "test/asakusa")
    }

    /* (non-Javadoc)
     * @see org.specs2.specification.After#after()
     */
    def after = {
      System.getProperties.remove("ASAKUSA_HOME")
    }
  }

  "JobQueueController" should {

    "POST /jobs must return OK" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "batchId" -> toJson("batchId"),
          "flowId" -> toJson("flowId"),
          "executionId" -> toJson("executionId"),
          "phaseId" -> toJson("phaseId"),
          "stageId" -> toJson("stageId"),
          "mainClass" -> toJson("mainClass"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val result = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("initialized")
        (contents \ "jrid").asOpt[String] must not none
      }
    } tag ("post-ok")

    "POST /jobs must return BadRequest without batchId" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "flowId" -> toJson("flowId"),
          "executionId" -> toJson("executionId"),
          "phaseId" -> toJson("phaseId"),
          "stageId" -> toJson("stageId"),
          "mainClass" -> toJson("mainClass"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val result = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))

        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("error")
        (contents \ "errorCode").as[String] must equalTo("400")
        (contents \ "errorMessage").as[String] must equalTo("No batchId specified.")
      }
    } tag ("without-batchId")

    "POST /jobs must return BadRequest without flowId" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "batchId" -> toJson("batchId"),
          "executionId" -> toJson("executionId"),
          "phaseId" -> toJson("phaseId"),
          "stageId" -> toJson("stageId"),
          "mainClass" -> toJson("mainClass"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val result = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))

        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("error")
        (contents \ "errorCode").as[String] must equalTo("400")
        (contents \ "errorMessage").as[String] must equalTo("No flowId specified.")
      }
    } tag ("without-flowId")

    "POST /jobs must return BadRequest without executionId" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "batchId" -> toJson("batchId"),
          "flowId" -> toJson("flowId"),
          "phaseId" -> toJson("phaseId"),
          "stageId" -> toJson("stageId"),
          "mainClass" -> toJson("mainClass"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val result = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))

        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("error")
        (contents \ "errorCode").as[String] must equalTo("400")
        (contents \ "errorMessage").as[String] must equalTo("No executionId specified.")
      }
    } tag ("without-executionId")

    "POST /jobs must return BadRequest without phaseId" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "batchId" -> toJson("batchId"),
          "flowId" -> toJson("flowId"),
          "executionId" -> toJson("executionId"),
          "stageId" -> toJson("stageId"),
          "mainClass" -> toJson("mainClass"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val result = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))

        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("error")
        (contents \ "errorCode").as[String] must equalTo("400")
        (contents \ "errorMessage").as[String] must equalTo("No phaseId specified.")
      }
    } tag ("without-phaseId")

    "POST /jobs must return BadRequest without stageId" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "batchId" -> toJson("batchId"),
          "flowId" -> toJson("flowId"),
          "executionId" -> toJson("executionId"),
          "phaseId" -> toJson("phaseId"),
          "mainClass" -> toJson("mainClass"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val result = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))

        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("error")
        (contents \ "errorCode").as[String] must equalTo("400")
        (contents \ "errorMessage").as[String] must equalTo("No stageId specified.")
      }
    } tag ("without-stageId")

    "POST /jobs must return BadRequest without mainClass" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "batchId" -> toJson("batchId"),
          "flowId" -> toJson("flowId"),
          "executionId" -> toJson("executionId"),
          "phaseId" -> toJson("phaseId"),
          "stageId" -> toJson("stageId"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val result = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))

        status(result) must equalTo(BAD_REQUEST)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("error")
        (contents \ "errorCode").as[String] must equalTo("400")
        (contents \ "errorMessage").as[String] must equalTo("No mainClass specified.")
      }
    } tag ("without-mainClass")

    "GET /jobs/:jrid must return status" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "batchId" -> toJson("batchId"),
          "flowId" -> toJson("flowId"),
          "executionId" -> toJson("executionId"),
          "phaseId" -> toJson("phaseId"),
          "stageId" -> toJson("stageId"),
          "mainClass" -> toJson("mainClass"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val register = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))
        val jrid = (parse(contentAsString(register)) \ "jrid").as[String]

        val info = JobQueueController.info(jrid)(FakeRequest(GET, "/jobs/" + jrid))
        status(info) must equalTo(OK)
        contentType(info) must beSome("application/json")
        charset(info) must beSome("utf-8")

        val contents = parse(contentAsString(info))
        (contents \ "status").as[String] must equalTo("initialized")
      }
    } tag ("get")

    "GET /jobs/:jrid must return NotFound with not exisiting jrid" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = JobQueueController.info("jrid")(FakeRequest(GET, "/jobs/jrid"))
        status(result) must equalTo(NOT_FOUND)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("error")
        (contents \ "errorCode").as[String] must equalTo("404")
        (contents \ "errorMessage").as[String] must equalTo("Specified jrid was not found.")
      }
    } tag ("get-notfound")

    "PUT /jobs/:jrid/execute must return status" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val params = toJson(Map(
          "batchId" -> toJson("batchId"),
          "flowId" -> toJson("flowId"),
          "executionId" -> toJson("executionId"),
          "phaseId" -> toJson("phaseId"),
          "stageId" -> toJson("stageId"),
          "mainClass" -> toJson("mainClass"),
          "arguments" -> toJson(Map("arg1" -> "avalue1")),
          "properties" -> toJson(Map("prop1" -> "pvalue1")),
          "env" -> toJson(Map("env1" -> "evalue1"))))
        val register = JobQueueController.register(FakeRequest(POST, "/jobs", FakeHeaders(), params))

        val jrid = (parse(contentAsString(register)) \ "jrid").as[String]

        val execute = JobQueueController.execute(jrid)(FakeRequest(PUT, "/jobs/" + jrid + "/execute"))
        status(execute) must equalTo(OK)
        contentType(execute) must beSome("application/json")
        charset(execute) must beSome("utf-8")

        val contents = parse(contentAsString(execute))
        (contents \ "status").as[String] must equalTo("waiting")
        (contents \ "jrid").as[String] must equalTo(jrid)

        while ((parse(contentAsString(JobQueueController.info(jrid)(FakeRequest(GET, "/jobs/" + jrid)))) \ "status").as[String] != "completed") {
          Thread.sleep(100)
        }

        Thread.sleep(1000)

        val deleted = parse(contentAsString(JobQueueController.info(jrid)(FakeRequest(GET, "/jobs/" + jrid))))
        (deleted \ "errorCode").as[String] must equalTo("404")
        (deleted \ "errorMessage").as[String] must equalTo("Specified jrid was not found.")
      }
    } tag ("execute")

    "PUT /jobs/:jrid/execute must return NotFound with not exisiting jrid" in TestContext {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = JobQueueController.execute("jrid")(FakeRequest(PUT, "/jobs/jrid/execute"))
        status(result) must equalTo(NOT_FOUND)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")

        val contents = parse(contentAsString(result))
        (contents \ "status").as[String] must equalTo("error")
        (contents \ "errorCode").as[String] must equalTo("404")
        (contents \ "errorMessage").as[String] must equalTo("Specified jrid was not found.")
      }
    } tag ("execute-notfound")
  } section ("JobQueueController")

}

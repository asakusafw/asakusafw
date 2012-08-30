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

import org.specs2.mutable._
import org.specs2.runner._
import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.util._
import akka.util.duration._
import akka.testkit._
import java.util.concurrent.TimeUnit

import org.junit.runner._

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
@RunWith(classOf[JUnitRunner])
class JobQueueSpec extends TestKit(ActorSystem("JobQueueSpec")) with Specification with Tags {

  implicit val timeout = Timeout(10, TimeUnit.SECONDS)

  "TestJobQueue" should {
    "REGISTER must register TestJobInfo(1, data, Initialized, None)" in {
      val actorRef = TestActorRef(new TestJobQueue(1, 100))

      val register = (actorRef ? REGISTER(TestJobInfo(0, "data", null, None))).mapTo[JOB_INFO[TestJobInfo]]
      val JOB_INFO(result) = Await.result(register, timeout.duration)

      result.jrid must equalTo(1)
      result.value must equalTo("data")
      result.status must equalTo(Initialized)
      result.exitCode must beNone
    } tag ("register")

    "INFO(5) must return None" in {
      val actorRef = TestActorRef(new TestJobQueue(1, 100))

      val info = (actorRef ? INFO(5)).mapTo[JOB_INFO_OPT[TestJobInfo]]
      val JOB_INFO_OPT(result) = Await.result(info, timeout.duration)

      result must beNone
    } tag ("info5")

    "INFO(10) must return Some(TestJobInfo(10, value10, Completed, Some(0)))" in {
      val actorRef = TestActorRef(new TestJobQueue(1, 100))

      val info = (actorRef ? INFO(10)).mapTo[JOB_INFO_OPT[TestJobInfo]]
      val JOB_INFO_OPT(result) = Await.result(info, timeout.duration)

      result must beSome(TestJobInfo(10, "value10", Completed, Some("0")))
    } tag ("info10")

    "EXECUTE(5) must return None" in {
      val actorRef = TestActorRef(new TestJobQueue(1, 100))

      val execute = (actorRef ? EXECUTE(5)).mapTo[JOB_INFO_OPT[TestJobInfo]]
      val JOB_INFO_OPT(result) = Await.result(execute, timeout.duration)

      result must beNone
    } tag ("execute5")

    "EXECUTE(10) must return Some(TestJobInfo(10, value10, Completed, Some(0))" in {
      val actorRef = TestActorRef(new TestJobQueue(1, 100))

      val execute = (actorRef ? EXECUTE(10)).mapTo[JOB_INFO_OPT[TestJobInfo]]
      val JOB_INFO_OPT(result) = Await.result(execute, timeout.duration)

      result must beSome(TestJobInfo(10, "value10", Completed, Some("0")))
    } tag ("execute10")

    "EXECUTE(20) must return Some(TestJobInfo(20, value20, Waiting, None))\n" +
      " and then    TestJobInfo(20, value20, Running, None)\n" +
      " and finally TestJobInfo(20, value20, Completed, Some(TestJobInfo(20,value20,Running,None)))" in {
        val actorRef = TestActorRef(new TestJobQueue(1, 5000))

        val execute = (actorRef ? EXECUTE(20)).mapTo[JOB_INFO_OPT[TestJobInfo]]
        val JOB_INFO_OPT(result) = Await.result(execute, timeout.duration)

        result must beSome(TestJobInfo(20, "value20", Waiting, None))

        Thread.sleep(2500)

        // running 2500

        val info1 = (actorRef ? INFO(20)).mapTo[JOB_INFO_OPT[TestJobInfo]]
        val JOB_INFO_OPT(result1) = Await.result(info1, timeout.duration)

        result1 must beSome(TestJobInfo(20, "value20", Running, None))

        Thread.sleep(5000)

        // completed 2500

        val info2 = (actorRef ? INFO(20)).mapTo[JOB_INFO_OPT[TestJobInfo]]
        val JOB_INFO_OPT(result2) = Await.result(info2, timeout.duration)

        result2 must beSome(TestJobInfo(20, "value20", Completed, Some("TestJobInfo(20,value20,Running,None)")))

        Thread.sleep(5000)

        // deleted 2500

        val info3 = (actorRef ? INFO(20)).mapTo[JOB_INFO_OPT[TestJobInfo]]
        val JOB_INFO_OPT(result3) = Await.result(info3, timeout.duration)

        result3 must beNone
      } tag ("execute20")

    "EXECUTE(1) and EXECUTE(20) must take 10 seconds" in {
      val actorRef = TestActorRef(new TestJobQueue(1, 5000))
      actorRef ! REGISTER(TestJobInfo(0, "data", null, None))

      val JOB_INFO_OPT(execute01_1) = Await.result((actorRef ? EXECUTE(1)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)
      val JOB_INFO_OPT(execute20_1) = Await.result((actorRef ? EXECUTE(20)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)

      execute01_1 must beSome(TestJobInfo(1, "data", Waiting, None))
      execute20_1 must beSome(TestJobInfo(20, "value20", Waiting, None))

      Thread.sleep(2500)

      // running 01 2500, waiting 20

      val JOB_INFO_OPT(execute01_2) = Await.result((actorRef ? INFO(1)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)
      val JOB_INFO_OPT(execute20_2) = Await.result((actorRef ? INFO(20)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)

      execute01_2 must beSome(TestJobInfo(1, "data", Running, None))
      execute20_2 must beSome(TestJobInfo(20, "value20", Waiting, None))

      Thread.sleep(5000)

      // completed 01 2500, running 20 2500

      val JOB_INFO_OPT(execute01_3) = Await.result((actorRef ? INFO(1)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)
      val JOB_INFO_OPT(execute20_3) = Await.result((actorRef ? INFO(20)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)

      execute01_3 must beSome(TestJobInfo(1, "data", Completed, Some("TestJobInfo(1,data,Running,None)")))
      execute20_3 must beSome(TestJobInfo(20, "value20", Running, None))

      Thread.sleep(5000)

      // deleted 01 2500, completed 20 2500

      val JOB_INFO_OPT(execute01_4) = Await.result((actorRef ? INFO(1)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)
      val JOB_INFO_OPT(execute20_4) = Await.result((actorRef ? INFO(20)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)

      execute01_4 must beNone
      execute20_4 must beSome(TestJobInfo(20, "value20", Completed, Some("TestJobInfo(20,value20,Running,None)")))

      Thread.sleep(5000)

      // deleted 01 7500, delted 20 2500

      val JOB_INFO_OPT(execute01_5) = Await.result((actorRef ? INFO(1)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)
      val JOB_INFO_OPT(execute20_5) = Await.result((actorRef ? INFO(20)).mapTo[JOB_INFO_OPT[TestJobInfo]], timeout.duration)

      execute01_5 must beNone
      execute20_5 must beNone
    } tag ("execute0120")
  } section ("TestJobQueue")

}

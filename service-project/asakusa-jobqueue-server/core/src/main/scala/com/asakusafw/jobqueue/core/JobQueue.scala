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

import org.slf4j.LoggerFactory

import akka.actor._
import akka.routing.SmallestMailboxRouter
import akka.util.duration._

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
trait JobQueue[ID, EC, JI <: JobInfo[ID, EC]] extends Actor {

  val Logger = LoggerFactory.getLogger(getClass)

  val nrWorkers: Int

  val completedJobDuration: Long

  def newWorker(): JobWorker[ID, EC, JI]

  protected val workers = context.actorOf(Props(new Invoker).withRouter(SmallestMailboxRouter(nrWorkers)))

  private var running = 0

  /* (non-Javadoc)
   * @see akka.actor.Actor#receive()
   */
  final def receive = {
    receiving orElse invoking
  }

  /**
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def receiving: Receive = {
    case REGISTER(jobInfo: JI) =>
      if (Logger.isInfoEnabled) Logger.info("Received REGISTER(" + jobInfo + ")")
      val registered = register(jobInfo)
      if (Logger.isInfoEnabled) Logger.info("Registered JOB_INFO(" + registered + ")")
      sender ! JOB_INFO[JI](registered)
    case INFO(jrid: ID) =>
      if (Logger.isDebugEnabled) Logger.debug("Received INFO(" + jrid + ")")
      sender ! JOB_INFO_OPT[JI](info(jrid))
    case EXECUTE(jrid: ID) =>
      if (Logger.isInfoEnabled) Logger.info("Received EXECUTE(" + jrid + ")")
      sender ! JOB_INFO_OPT[JI](execute(jrid))
    case DELETE(jrid: ID) =>
      if (Logger.isInfoEnabled) Logger.info("Received DELETE(" + jrid + ")")
      sender ! JOB_INFO_OPT[JI](delete(jrid))

    case COMPLETE(jobContext, exitCode) =>
      if (Logger.isInfoEnabled) Logger.info("Received COMPLETE(JobContext(" + jobContext.jobInfo + "), " + exitCode + ")")
      complete(jobContext, exitCode)
  }

  /**
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def invoking: Receive = {
    case INVOKE(jobInfo: JI) =>
      if (Logger.isInfoEnabled) Logger.info("Received INVOKE(" + jobInfo + ")")
      invoke(jobInfo)
  }

  /**
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def waiting: Receive = {
    case INVOKE(jobInfo: JI) =>
      if (Logger.isInfoEnabled) Logger.info("Received INVOKE(" + jobInfo + ") but JobQueue is full.")
      context.system.scheduler.scheduleOnce(50 millis, self, INVOKE(jobInfo))
  }

  /**
   * @param jobInfo
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def register(jobInfo: JI): JI = {
    jobInfo
  }

  /**
   * @param jrid
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def info(jrid: ID): Option[JI] = {
    None
  }

  /**
   * @param jrid
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def execute(jrid: ID): Option[JI] = {
    info(jrid) map { jobInfo =>
      self ! INVOKE(jobInfo)
      jobInfo
    }
  }

  /**
   * @param jrid
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def delete(jrid: ID): Option[JI] = {
    None
  }

  /**
   * @param jobInfo
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def invoke(jobInfo: JI): Unit = {
    running += 1
    if (running >= nrWorkers) {
      if (Logger.isInfoEnabled) Logger.info("Reached running workers to " + nrWorkers + " so waiting to finish...")
      context.become(receiving orElse waiting)
    }
  }

  /**
   * @param jobContext
   * @param exitCode
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  protected def complete(jobContext: JobContext[ID, EC, JI], exitCode: EC): Unit = {
    context.system.scheduler.scheduleOnce(completedJobDuration millis, self, DELETE(jobContext.jobInfo.jrid))
    if (running == nrWorkers) {
      if (Logger.isInfoEnabled) Logger.info("Fall running workers below " + nrWorkers + ".")
      context.unbecome()
    }
    running -= 1
  }

  /**
   * TODO JobQueue document
   * @author ueshin
   * @since TODO
   */
  protected sealed trait InvokerMessage
  protected case class INVOKE_WORKER(jobContext: JobContext[ID, EC, JI]) extends InvokerMessage
  protected case class COMPLETE(jobContext: JobContext[ID, EC, JI], exitCode: EC) extends InvokerMessage

  /**
   * TODO JobQueue document
   * @author ueshin
   * @since TODO
   */
  private class Invoker extends Actor {

    /**
     * @return
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    def receive = {
      case INVOKE_WORKER(jobContext: JobContext[ID, EC, JI]) =>
        val exitCode = newWorker().run(jobContext)
        sender ! COMPLETE(jobContext, exitCode)
    }
  }

}

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

import com.typesafe.config._

import akka.util.Timeout

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
object Settings {

  val JobQueueProfile = "/jobqueue/conf/jobqueue.properties"

  val ConfAsakusaHomeKey = "asakusa.home"

  val ConfNRWorkerKey = "core.worker"
  val ConfCompletedJobDuration = "core.completedjobduration"
  val ConfHadoopLogDir = "hadoop.log.dir"

  val ConfJobQueueAskTimeout = "jobqueue.askTimeout"

  lazy val asakusaHome = ConfigFactory.load().getString(ConfAsakusaHomeKey)
  lazy val config = ConfigFactory.load(ConfigFactory.parseFile(new File(asakusaHome + JobQueueProfile),
    ConfigParseOptions.defaults.setSyntax(ConfigSyntax.CONF)))

  lazy val nrWorker = config.getInt(ConfNRWorkerKey)
  lazy val completedJobDuration = config.getLong(ConfCompletedJobDuration)

  lazy val hadoopLogDir = new File(config.getString(ConfHadoopLogDir))

  lazy val jobQueueAskTimeout = Timeout(config.getMilliseconds(ConfJobQueueAskTimeout))
}

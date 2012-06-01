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
import java.sql.Connection
import java.util.UUID

import com.asakusafw.jobqueue.core._

import akka.util.duration._
import anorm.{ Error => _, _ }
import play.api.Play.current
import play.api.db.DB

/**
 * TODO  document
 * @author ueshin
 * @since TODO
 */
class HadoopJobQueue(
  val nrWorkers: Int, val completedJobDuration: Long,
  asakusaHome: File, hadoopLogDir: Option[File]) extends JobQueue[String, Int, HadoopJobInfo] {

  /**
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  def newWorker() = {
    new HadoopJobWorker(asakusaHome)
  }

  /**
   * @param jobInfo
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override def register(jobInfo: HadoopJobInfo) = {
    DB.withTransaction { implicit conn =>
      doRegister(jobInfo)
    }
  }

  /**
   * @param jobInfo
   * @param conn
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  private def doRegister(jobInfo: HadoopJobInfo)(implicit conn: Connection): HadoopJobInfo = {
    val jrid = UUID.randomUUID()
    SQL("""
          INSERT INTO JOB_QUEUE (
            jrid, batch_id, flow_id, execution_id, phase_id, stage_id, main_class, status
          ) VALUES (
            {jrid}, {batchId}, {flowId}, {executionId}, {phaseId}, {stageId}, {mainClass}, {status}
          )
        """).on(
      'jrid -> jrid,
      'batchId -> jobInfo.batchId,
      'flowId -> jobInfo.flowId,
      'executionId -> jobInfo.executionId,
      'phaseId -> jobInfo.phaseId,
      'stageId -> jobInfo.stageId,
      'mainClass -> jobInfo.mainClass,
      'status -> Initialized.name).executeUpdate()
    jobInfo.arguments.foreach { argument =>
      SQL("""
            INSERT INTO JOB_ARGUMENTS (
              jrid, name, value
            ) VALUES (
              {jrid}, {name}, {value}
            )
          """).on('jrid -> jrid, 'name -> argument._1, 'value -> argument._2).executeUpdate()
    }
    jobInfo.properties.foreach { property =>
      SQL("""
            INSERT INTO JOB_PROPERTIES (
              jrid, name, value
            ) VALUES (
              {jrid}, {name}, {value}
            )
          """).on('jrid -> jrid, 'name -> property._1, 'value -> property._2).executeUpdate()
    }
    jobInfo.env.foreach { e =>
      SQL("""
            INSERT INTO JOB_ENV (
              jrid, name, value
            ) VALUES (
              {jrid}, {name}, {value}
            )
          """).on('jrid -> jrid, 'name -> e._1, 'value -> e._2).executeUpdate()
    }
    doInfo(jrid.toString).get
  }

  /**
   * @param jrid
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override def info(jrid: String) = {
    DB.withTransaction { implicit conn =>
      doInfo(jrid)
    }
  }

  /**
   * @param jrid
   * @param conn
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  private def doInfo(jrid: String)(implicit conn: Connection): Option[HadoopJobInfo] = {
    SQL("""
          SELECT * FROM JOB_QUEUE WHERE jrid = {jrid}
        """).on('jrid -> jrid)().headOption.map { head =>
      HadoopJobInfo(
        jrid,
        head[String]("batch_id"),
        head[String]("flow_id"),
        head[String]("execution_id"),
        head[String]("phase_id"),
        head[String]("stage_id"),
        head[String]("main_class"),
        JobStatus.valueOf(head[String]("status")).get,
        head[Option[Int]]("exit_code"),
        SQL("SELECT name, value FROM JOB_ARGUMENTS WHERE jrid = {jrid}").on('jrid -> jrid)().map { row =>
          row[String]("name") -> row[String]("value")
        }.toMap,
        SQL("SELECT name, value FROM JOB_PROPERTIES WHERE jrid = {jrid}").on('jrid -> jrid)().map { row =>
          row[String]("name") -> row[String]("value")
        }.toMap,
        SQL("SELECT name, value FROM JOB_ENV WHERE jrid = {jrid}").on('jrid -> jrid)().map { row =>
          row[String]("name") -> row[String]("value")
        }.toMap)
    }
  }

  /**
   * @param jrid
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override protected def execute(jrid: String) = {
    DB.withTransaction { implicit conn =>
      doExecute(jrid)
    }
  }

  /**
   * @param jrid
   * @param conn
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  private def doExecute(jrid: String)(implicit conn: Connection): Option[HadoopJobInfo] = {
    doInfo(jrid).map { jobInfo =>
      jobInfo.status match {
        case Initialized =>
          SQL("""
                UPDATE JOB_QUEUE SET status = {status} WHERE jrid = {jrid}
              """).on('status -> Waiting.name, 'jrid -> jobInfo.jrid).executeUpdate()
          val updated = doInfo(jrid).get
          self ! INVOKE(updated)
          updated
        case _ => jobInfo
      }
    }
  }

  /**
   * @param jrid
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override protected def delete(jrid: String): Option[HadoopJobInfo] = {
    DB.withTransaction { implicit conn =>
      doDelete(jrid)
    }
  }

  /**
   * @param jrid
   * @param conn
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  private def doDelete(jrid: String)(implicit conn: Connection): Option[HadoopJobInfo] = {
    doInfo(jrid).map { jobInfo =>
      if (jobInfo.status == Initialized || jobInfo.status == Completed || jobInfo.status == Error) {
        SQL("""
              DELETE FROM JOB_QUEUE WHERE jrid = {jrid}
            """).on('jrid -> jobInfo.jrid).executeUpdate()
        SQL("""
              DELETE FROM JOB_ARGUMENTS WHERE jrid = {jrid}
            """).on('jrid -> jobInfo.jrid).executeUpdate()
        SQL("""
              DELETE FROM JOB_PROPERTIES WHERE jrid = {jrid}
            """).on('jrid -> jobInfo.jrid).executeUpdate()
        SQL("""
              DELETE FROM JOB_ENV WHERE jrid = {jrid}
            """).on('jrid -> jobInfo.jrid).executeUpdate()
        HadoopJobInfo(jobInfo.jrid, jobInfo.batchId, jobInfo.flowId, jobInfo.executionId, jobInfo.phaseId, jobInfo.stageId, jobInfo.mainClass,
          Deleted, jobInfo.exitCode, jobInfo.arguments, jobInfo.properties, jobInfo.env)
      } else jobInfo
    }
  }

  /**
   * @param jobInfo
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override protected def invoke(jobInfo: HadoopJobInfo) = {
    DB.withTransaction { implicit conn =>
      doInvoke(jobInfo)
    }
  }

  /**
   * @param jobInfo
   * @param conn
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  private def doInvoke(jobInfo: HadoopJobInfo)(implicit conn: Connection): Unit = {
    SQL("""
          UPDATE JOB_QUEUE SET status = {status} WHERE jrid = {jrid}
        """).on('status -> Running.name, 'jrid -> jobInfo.jrid).executeUpdate()
    doInfo(jobInfo.jrid) map { jobInfo =>
      val jobContext = hadoopLogDir.map { dir =>
        val parent = new File(dir, jobInfo.jrid)
        parent.mkdirs()
        JobContext[String, Int, HadoopJobInfo](jobInfo,
          out = new FileOutputStream(new File(parent, "stdout")), err = new FileOutputStream(new File(parent, "stderr")))
      }.getOrElse(JobContext[String, Int, HadoopJobInfo](jobInfo))
      workers ! INVOKE_WORKER(jobContext)
      super.invoke(jobInfo)
    }
  }

  /**
   * @param jobContext
   * @param exitCode
   * @return
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  override protected def complete(jobContext: JobContext[String, Int, HadoopJobInfo], exitCode: Int) = {
    DB.withTransaction { implicit conn =>
      doComplete(jobContext, exitCode)
    }
  }

  /**
   * @param jobContext
   * @param exitCode
   * @param conn
   * @throws IllegalArgumentException if any parameter is {@code null}
   */
  private def doComplete(jobContext: JobContext[String, Int, HadoopJobInfo], exitCode: Int)(implicit conn: Connection): Unit = {
    SQL("""
          UPDATE JOB_QUEUE SET status = {status}, exit_code = {exitCode} WHERE jrid = {jrid}
        """).on(
      'status -> Completed.name,
      'exitCode -> exitCode,
      'jrid -> jobContext.jobInfo.jrid).executeUpdate()
    super.complete(jobContext, exitCode)
  }

}

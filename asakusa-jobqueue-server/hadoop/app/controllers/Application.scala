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

import play.api.libs.json.Json._
import play.api.mvc._
import models.Settings

/**
 * TODO  document
 * @since TODO
 */
object Application extends Controller {

  def index = Action {
    Ok(toJson(Map(
      "application" -> toJson("asakusa-jobqueue"),
      "configurations" -> toJson(Map(
        "ASAKUSA_HOME" -> toJson(Settings.asakusaHome),
        Settings.ConfNRWorkerKey -> toJson(Settings.nrWorker),
        Settings.ConfHadoopLogDir -> toJson(Settings.hadoopLogDir.getAbsolutePath))))))
  }

}

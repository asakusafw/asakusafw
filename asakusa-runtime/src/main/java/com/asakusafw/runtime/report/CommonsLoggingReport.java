/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.report;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.core.Report;
import com.asakusafw.runtime.core.Report.Level;

/**
 * Report API via Commons Logging.
 * @since 0.2.6
 */
public class CommonsLoggingReport extends Report.Delegate {

    static final Log LOG = LogFactory.getLog(CommonsLoggingReport.class);

    @Override
    protected void report(Level level, String message) {
        if (level == Level.ERROR) {
            if (LOG.isErrorEnabled()) {
                LOG.error(message, new Exception("error"));
            }
        } else if (level == Level.WARN) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(message, new Exception("warn"));
            }
        } else if (level == Level.INFO) {
            LOG.info(message);
        } else {
            LOG.fatal(MessageFormat.format("Unknown level \"{0}\": {1}", level, message));
        }
    }

}

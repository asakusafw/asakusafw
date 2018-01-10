/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver.executor;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.basic.BasicDeleteTaskExecutor;
import com.asakusafw.workflow.model.DeleteTaskInfo;

/**
 * Executes {@link DeleteTaskInfo} directly.
 * @since 0.10.0
 */
public class DefaultDeleteTaskExecutor extends BasicDeleteTaskExecutor {

    static final Logger LOG = LoggerFactory.getLogger(DefaultDeleteTaskExecutor.class);

    @Override
    protected void deleteOnHadoopFileSystem(TaskExecutionContext context, String path) throws IOException {
        Configuration conf = context.findResource(Configuration.class).get();
        Path p = new Path(path);
        FileSystem fs = p.getFileSystem(conf);
        try {
            fs.delete(p, true);
        } catch (FileNotFoundException e) {
            // may not occur
            LOG.debug("unexpected exception was occurred", e);
        }
    }
}

/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.inprocess;

import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.inprocess.InProcessStageConfigurator;
import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;
import com.asakusafw.runtime.stage.launcher.LauncherOptionsParser;
import com.asakusafw.runtime.stage.optimizer.LibraryCopySuppressionConfigurator;
import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.HadoopTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * Executes {@link HadoopTaskInfo} in this process.
 * @since 0.10.0
 */
public class InProcessHadoopTaskExecutor implements TaskExecutor {

    static final Logger LOG = LoggerFactory.getLogger(InProcessHadoopTaskExecutor.class);

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return task instanceof HadoopTaskInfo
                && TaskExecutors.findFrameworkHome(context).filter(Files::isDirectory).isPresent();
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) throws InterruptedException, IOException {
        HadoopTaskInfo mirror = (HadoopTaskInfo) task;

        List<String> arguments = new ArrayList<>();
        arguments.add(mirror.getClassName());

        Map<String, String> properties = getHadoopProperties(context);

        TaskExecutors.withLibraries(context, classLoader -> {
            Configuration conf = context.findResource(Configuration.class).get();
            TaskExecutors.findCoreConfigurationUrl(context)
                .ifPresent(conf::addResource);

            properties.forEach(conf::set);
            conf.setClassLoader(classLoader);

            LOG.info("starting Hadoop task: {} {}", mirror.getClassName());
            int exit = ApplicationLauncher.exec(conf, arguments.toArray(new String[arguments.size()]));
            if (exit != 0) {
                throw new IOException(MessageFormat.format(
                        "failed to execute Hadoop task: class={0}, exit={1}",
                        mirror.getClassName(),
                        exit));
            }
        });
    }

    private static Map<String, String> getHadoopProperties(TaskExecutionContext context) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(StageConstants.PROP_USER, TaskExecutors.getUserName(context));
        properties.put(StageConstants.PROP_EXECUTION_ID, context.getExecutionId());
        properties.put(StageConstants.PROP_ASAKUSA_BATCH_ARGS,
                TaskExecutors.resolveCommandToken(context, CommandToken.BATCH_ARGUMENTS));
        properties.put(LauncherOptionsParser.KEY_CACHE_ENABLED, String.valueOf(false));
        properties.put(LibraryCopySuppressionConfigurator.KEY_ENABLED, String.valueOf(true));
        properties.put(InProcessStageConfigurator.KEY_FORCE, String.valueOf(true));
        properties.putAll(context.getConfigurations());
        return properties;
    }
}

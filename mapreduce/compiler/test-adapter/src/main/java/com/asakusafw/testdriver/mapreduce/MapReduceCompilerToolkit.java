/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.testdriver.mapreduce;

import java.io.File;
import java.io.IOException;

import com.asakusafw.testdriver.compiler.CompilerConfiguration;
import com.asakusafw.testdriver.compiler.CompilerSession;
import com.asakusafw.testdriver.compiler.CompilerToolkit;
import com.asakusafw.testdriver.compiler.FlowPortMap;
import com.asakusafw.testdriver.compiler.util.DeploymentUtil;

/**
 * An implementation of {@link CompilerToolkit} for the MapReduce compiler.
 * @since 0.8.0
 */
public class MapReduceCompilerToolkit implements CompilerToolkit {

    @Override
    public String getName() {
        return "MapReduce";
    }

    @Override
    public CompilerConfiguration newConfiguration() {
        MapReduceCompilerConfiguration configuration = new MapReduceCompilerConfiguration();
        configuration.withDefaults();
        return configuration;
    }

    @Override
    public FlowPortMap newFlowPortMap() {
        return new MapReduceFlowPortMap();
    }

    @Override
    public CompilerSession newSession(CompilerConfiguration configuration) throws IOException {
        if ((configuration instanceof MapReduceCompilerConfiguration) == false) {
            throw new IllegalArgumentException();
        }
        MapReduceCompilerConfiguration conf = (MapReduceCompilerConfiguration) configuration;
        initializeWorkingDirectory(conf);
        return new MapReduceCompilerSession(conf);
    }

    private void initializeWorkingDirectory(CompilerConfiguration conf) throws IOException {
        File workingDirectory = conf.getWorkingDirectory();
        if (workingDirectory == null) {
            throw new IllegalStateException();
        }
        if (workingDirectory.exists()) {
            DeploymentUtil.delete(workingDirectory);
        }
    }
}

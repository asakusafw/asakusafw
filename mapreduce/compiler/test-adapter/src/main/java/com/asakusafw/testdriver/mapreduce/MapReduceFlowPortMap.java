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

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.testdriver.compiler.CompilerConstants;
import com.asakusafw.testdriver.compiler.FlowPortMap;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

class MapReduceFlowPortMap implements FlowPortMap {

    private final FlowDescriptionDriver driver = new FlowDescriptionDriver();

    MapReduceFlowPortMap() {
        return;
    }

    @Override
    public <T> In<T> addInput(String name, Class<T> dataType) {
        String basePath = CompilerConstants.getRuntimeWorkingDirectory();
        String path = MapReduceCompierUtil.createInputLocation(basePath, name).toPath('/');
        return driver.createIn(name, new DirectImporterDescription(dataType, path));
    }

    @Override
    public <T> Out<T> addOutput(String name, Class<T> dataType) {
        String basePath = CompilerConstants.getRuntimeWorkingDirectory();
        String path = MapReduceCompierUtil.createOutputLocation(basePath, name).toPath('/');
        return driver.createOut(name, new DirectExporterDescription(dataType, path));
    }

    public FlowGraph resolve(FlowDescription flowDescription) {
        return driver.createFlowGraph(flowDescription);
    }
}
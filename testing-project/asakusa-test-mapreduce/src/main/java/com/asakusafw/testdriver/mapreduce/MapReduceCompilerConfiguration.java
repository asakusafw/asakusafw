/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import com.asakusafw.compiler.batch.processor.DependencyLibrariesProcessor;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.testdriver.compiler.basic.BasicCompilerConfiguration;

class MapReduceCompilerConfiguration extends BasicCompilerConfiguration {

    public void withDefaults() {
        withClassLoader(getClass().getClassLoader());
        withOptimizeLevel(OptimizeLevel.NORMAL);
        withDebugLevel(DebugLevel.DISABLED);

        // copy dependency libraries manually
        withOption(DependencyLibrariesProcessor.KEY_ENABLE, GenericOptionValue.DISABLED.getSymbol());

        // disables Direct I/O input filters
        if (checkClass("com.asakusafw.compiler.directio.DirectFileIoProcessor")) { //$NON-NLS-1$
            withOption(
                    "directio.input.filter.enabled", //$NON-NLS-1$
                    GenericOptionValue.DISABLED.getSymbol());
        }
    }

    private boolean checkClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public FlowCompilerOptions getFlowCompilerOptions() {
        return MapReduceCompierUtil.toFlowCompilerOptions(getOptimizeLevel(), getDebugLevel(), getOptions());
    }
}

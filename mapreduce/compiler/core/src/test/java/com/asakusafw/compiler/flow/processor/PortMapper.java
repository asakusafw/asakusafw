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
package com.asakusafw.compiler.flow.processor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.testing.MockResult;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;


class PortMapper {

    private Fragment fragment;

    private Map<String, Result<?>> created = new HashMap<>();

    public PortMapper(Fragment fragment) {
        this.fragment = fragment;
    }

    public <T> MockResult<T> create(String name) {
        MockResult<T> result = MockResult.create();
        return add(name, result);
    }

    public <T extends Result<?>> T add(String name, T result) {
        created.put(name, result);
        return result;
    }

    public Object[] toArguments() {
        List<Result<?>> results = new ArrayList<>();
        for (FlowElementOutput out : fragment.getOutputPorts()) {
            String name = out.getDescription().getName();
            Result<?> port = created.get(name);
            assertThat(name + created, port, not(nullValue()));
            results.add(port);
        }
        return results.toArray();
    }
}

/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.hadoop.io.Text;

import com.asakusafw.testdriver.core.AbstractTestDataToolProvider;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.TestRule;
import com.asakusafw.testdriver.core.VerifyRuleFactory;

/**
 * Mock implementation of {@link AbstractTestDataToolProvider}.
 */
public class MockTestDataToolProvider extends AbstractTestDataToolProvider {

    @SuppressWarnings("unchecked")
    @Override
    public <T> DataModelDefinition<T> toDataModelDefinition(Class<T> dataModelClass) throws IOException {
        if (dataModelClass != Text.class) {
            throw new IOException();
        }
        return (DataModelDefinition<T>) new MockTextDefinition();
    }

    @Override
    public DataModelSourceFactory getDataModelSourceFactory(URI uri) {
        throw new AssertionError();
    }

    @Override
    public DataModelSinkFactory getDataModelSinkFactory(URI uri) {
        throw new AssertionError();
    }

    @Override
    public DifferenceSinkFactory getDifferenceSinkFactory(URI uri) {
        throw new AssertionError();
    }

    @Override
    public VerifyRuleFactory getVerifyRuleFactory(URI ruleUri, List<? extends TestRule> extraRules) {
        throw new AssertionError();
    }
}

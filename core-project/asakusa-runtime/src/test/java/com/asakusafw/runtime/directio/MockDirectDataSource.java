/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * A mock implementation of {@link AbstractDirectDataSource}.
 */
public class MockDirectDataSource extends AbstractDirectDataSource {

    /**
     * The configured profile.
     */
    public DirectDataSourceProfile profile;

    @Override
    public void configure(DirectDataSourceProfile p) throws IOException, InterruptedException {
        this.profile = p;
    }

    @Override
    public <T> List<DirectInputFragment> findInputFragments(
            DataDefinition<T> definition,
            String basePath,
            ResourcePattern resourcePattern) throws IOException, InterruptedException {
        return Collections.emptyList();
    }

    @Override
    public <T> ModelInput<T> openInput(
            DataDefinition<T> definition,
            DirectInputFragment fragment,
            Counter counter) throws IOException, InterruptedException {
        return new ModelInput<T>() {
            @Override
            public boolean readTo(T model) throws IOException {
                return false;
            }
            @Override
            public void close() throws IOException {
                return;
            }
        };
    }

    @Override
    public <T> ModelOutput<T> openOutput(
            OutputAttemptContext context,
            DataDefinition<T> definition,
            String basePath,
            String resourcePath, Counter counter) throws IOException, InterruptedException {
        return new ModelOutput<T>() {
            @Override
            public void write(T model) throws IOException {
                return;
            }
            @Override
            public void close() throws IOException {
                return;
            }
        };
    }

    @Override
    public List<ResourceInfo> list(
            String basePath,
            ResourcePattern resourcePattern,
            Counter counter) throws IOException, InterruptedException {
        return Collections.emptyList();
    }

    @Override
    public boolean delete(
            String basePath,
            ResourcePattern resourcePattern,
            boolean recursive,
            Counter counter) throws IOException, InterruptedException {
        return false;
    }

    @Override
    public void setupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        return;
    }

    @Override
    public void commitAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        return;
    }

    @Override
    public void cleanupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        return;
    }

    @Override
    public void setupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        return;
    }

    @Override
    public void commitTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        return;
    }

    @Override
    public void cleanupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        return;
    }
}

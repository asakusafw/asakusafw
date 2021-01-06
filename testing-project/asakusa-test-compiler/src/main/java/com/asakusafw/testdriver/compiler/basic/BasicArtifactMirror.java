/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.testdriver.compiler.basic;

import java.io.File;

import com.asakusafw.testdriver.compiler.ArtifactMirror;
import com.asakusafw.testdriver.compiler.BatchMirror;

/**
 * A basic implementation of {@link ArtifactMirror}.
 * @since 0.8.0
 */
public class BasicArtifactMirror extends AbstractMirror implements ArtifactMirror {

    private final BatchMirror batch;

    private final File contents;

    /**
     * Creates a new instance.
     * @param batch the batch
     * @param contents the contents root
     */
    public BasicArtifactMirror(BatchMirror batch, File contents) {
        this.batch = batch;
        this.contents = contents;
    }

    @Override
    public BatchMirror getBatch() {
        return batch;
    }

    @Override
    public File getContents() {
        return contents;
    }
}

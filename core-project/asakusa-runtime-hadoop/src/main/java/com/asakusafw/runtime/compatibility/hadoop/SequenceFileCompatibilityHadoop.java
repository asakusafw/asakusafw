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
package com.asakusafw.runtime.compatibility.hadoop;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.SequenceFile;

/**
 * Compatibility for {@link SequenceFile} APIs.
 * Clients should not use this class directly.
 * @since 0.7.4
 */
public abstract class SequenceFileCompatibilityHadoop implements CompatibilityHadoop {

    /**
     * Creates a new {@link SequenceFile} reader.
     * @param in the source
     * @param length the stream length
     * @param conf current configuration
     * @return the created sequence file reader
     * @throws IOException if failed to open the sequence file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public abstract SequenceFile.Reader openReader(
            InputStream in,
            long length,
            Configuration conf) throws IOException;
}

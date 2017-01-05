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
package com.asakusafw.compiler.directio;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.compiler.directio.testing.model.Line;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormatAdapter;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Mock {@link HadoopFileFormat}.
 */
public class LineFileFormat extends HadoopFileFormatAdapter<Line> {

    /**
     * Creates a new instance.
     */
    public LineFileFormat() {
        super(new LineFormat());
    }

    @Override
    public ModelInput<Line> createInput(Class<? extends Line> dataType, FileSystem fileSystem,
            Path path, long offset, long fragmentSize, Counter counter) throws IOException,
            InterruptedException {
        if (getConf() == null) {
            throw new IllegalStateException();
        }
        return super.createInput(dataType, fileSystem, path, offset, fragmentSize, counter);
    }

    @Override
    public ModelOutput<Line> createOutput(Class<? extends Line> dataType, FileSystem fileSystem,
            Path path, Counter counter) throws IOException, InterruptedException {
        if (getConf() == null) {
            throw new IllegalStateException();
        }
        return super.createOutput(dataType, fileSystem, path, counter);
    }
}

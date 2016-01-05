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
package com.asakusafw.testdriver.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormatAdapter;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Mock {@link HadoopFileFormat}.
 */
public class MockFileFormat extends HadoopFileFormatAdapter<Text> {

    /**
     * Creates a new instance.
     */
    public MockFileFormat() {
        super(new MockStreamFormat());
    }

    @Override
    public ModelInput<Text> createInput(Class<? extends Text> dataType, FileSystem fileSystem,
            Path path, long offset, long fragmentSize, Counter counter) throws IOException,
            InterruptedException {
        assertThat(getConf(), is(notNullValue()));
        return super.createInput(dataType, fileSystem, path, offset, fragmentSize, counter);
    }

    @Override
    public ModelOutput<Text> createOutput(Class<? extends Text> dataType, FileSystem fileSystem,
            Path path, Counter counter) throws IOException, InterruptedException {
        assertThat(getConf(), is(notNullValue()));
        return super.createOutput(dataType, fileSystem, path, counter);
    }
}
